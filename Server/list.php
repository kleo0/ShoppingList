<?php

require_once "connect.php";
require_once "error.php";
require_once "consts.php";
require_once "image_recognition.php";

const ACTIONS = ["new", "del", "mod", 'get', 'dir', 'share', 'process', 'stat_list_user','stat_list_product'];

$res = [
  'ERR' => ERR_OK,
  'JSON_DATA' => "{}"
];

/* Returns dump of list followed by id */
function get_list_products(PDO $dbh, $lid)
{
  $data = ["lid" => $lid, "users" => "[]", "products" => "[]"];

  // Get all products
  try {
    $sth = $dbh->prepare("select products.productname, list_elements.quantity from products, list_elements " .
      "where products.pid  = list_elements.pid and list_elements.lid = :lid");

    $sth->bindParam(':lid', $lid, PDO::PARAM_INT);
    $sth->execute();

    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    $products = [];
    foreach ($result as $r) {
      $elem = array('n' => $r['productname'], 'q' => $r['quantity']);
      array_push($products, $elem);
    }
    $data['products'] = $products;

  } catch (Exception $e) {
    die($e->getMessage());
  }

  // Get all list users
  try {
    $sth = $dbh->prepare("select users.nickname from users,list_membership where " .
      "users.uid = list_membership.uid and list_membership.lid = :lid");

    $sth->bindParam(':lid', $lid, PDO::PARAM_INT);
    $sth->execute();

    $result = $sth->fetchAll(PDO::FETCH_ASSOC);

    $users = [];
    foreach ($result as $r) {
      $elem = $r['nickname'];
      array_push($users, $elem);
    }
    $data['users'] = $users;

  } catch (Exception $e) {
    die($e->getMessage());
  }

  return $data;
}

/* Check user permission to RW on list */
function check_user_list_permission(PDO $dbh, $lid, $uid) {
  try {
    $sth = $dbh->prepare('select uid,lid from list_membership where lid=:lid and uid=:uid');
    $sth->bindParam(':lid', $lid, PDO::PARAM_INT);
    $sth->bindParam(':uid', $uid, PDO::PARAM_INT);
    $sth->execute();

    return $sth->rowCount() > 0;

  } catch (Exception $e) {
    return false;
  }
}

/* Share list with users */
function share_list_with_users(PDO $dbh, $lid, array $users) {
  // get id of all users is
  $user_ids = null;
  try {
    $inQuery = implode(',', array_fill(0, count($users), '?'));
    $sth = $dbh->prepare("SELECT uid FROM users WHERE nickname IN (" . $inQuery . ")");
    foreach ($users as $i => $username) {
      $sth->bindValue($i + 1, $username, PDO::PARAM_STR);
    }
    $sth->execute();
    $user_ids = $sth->fetchAll(PDO::FETCH_NUM);

  } catch (Exception $e) {
    error_log($e->getMessage());
    $res['ERR'] = ERR_INPUT_DATA_MALFORMED;
    finish($res);
  }

  // add all users to list in db
  try {
    $inQuery = implode(',', array_fill(0, count($user_ids), "($lid,?)"));
    $sth = $dbh->prepare("INSERT INTO list_membership(lid,uid) VALUES " . $inQuery);
    foreach ($user_ids as $i => $user_id) {
      $sth->bindValue($i + 1, $user_id[0]);
    }
    $sth->execute();

  } catch (Exception $e) {
    error_log($e->getMessage());
    $res['ERR'] = ERR_INPUT_DATA_MALFORMED;
    finish($res);
  }
}

/* Determine product ids basing on product names
 * Returns map [name,id] with only found products
 */
function get_products_id(PDO $dbh, array $product_names) {
  $ids = [];

  try {
    // use dummy check (1-1) for now, upgrade later
    $inQuery = implode(',', array_fill(0, count($product_names), '?'));
    $sth = $dbh->prepare('SELECT pid,productname FROM products WHERE productname IN (' . $inQuery . ')');

    foreach ($product_names as $i => $product_name) {
      $sth->bindValue($i + 1, $product_name, PDO::PARAM_STR);
    }
    $sth->execute();

    $query_results = $sth->fetchAll(PDO::FETCH_ASSOC);
    foreach ($query_results as $query_result) {
      $ids[$query_result['productname']] = $query_result['pid'];
    }

  } catch (Exception $e) {
    error_log($e);
  }

  return $ids;
}

function get_create_pids_from_names(PDO $dbh, array $mixed_products) {
  $product_names = [];
  foreach ($mixed_products as $product) {
    array_push($product_names, $product['n']);
  }
  $ids = get_products_id($dbh, $product_names);

  // get unknown products, diff between names, and keys (names) of known products
  $unknown_products = array_diff($product_names, array_keys($ids));

  // insert unknown products & get their ids
  foreach ($unknown_products as $unknown_product) {
    try {
      $sth = $dbh->prepare("INSERT INTO products(productname) VALUES (:productname)");
      $sth->bindParam(':productname', $unknown_product, PDO::PARAM_STR, MAX_PRODUCT_LEN);
      $sth->execute();
      $ids[$unknown_product] = $dbh->lastInsertId();

    } catch (Exception $e) {
      error_log($e->getMessage());
      $res['ERR'] = ERR_INPUT_DATA_MALFORMED;
      finish($res);
    }
  }

  return $ids;
}

/* standard script */
if (!$db_ok) {
  $res['ERR'] = ERR_DATABASE;
  finish($res);
}

if (empty($_POST['token']) || empty($_POST['action']) || empty($_POST['data'])) {
  $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
  finish($res);
}

$token = urldecode($_POST['token']);
$token = str_replace("\"", "", $token);
$action = $_POST['action'];
$uid = -123;

/* Verify correctness of JSON data */
error_log($_POST['data']);
$data = json_decode($_POST['data'], true);
if ($data === null && json_last_error() !== JSON_ERROR_NONE) {
  error_log(json_last_error_msg());
  error_log($_POST['data']);
  $res['ERR'] = ERR_INPUT_DATA_MALFORMED;
  finish($res);
}

/* Login & get uid, trusted token */
try {
  $sth = $dbh->prepare("select uid,token from logged_users where token = :token");
  $sth->bindParam(':token', $token, PDO::PARAM_STR);
  $sth->execute();

  if ($sth->rowCount() != 1) {
    error_log("Cannot login with token: " . $token);
    throw new Exception("User is not logged");
  } else {
    $result = $sth->fetch(PDO::FETCH_ASSOC);
    $token = $result['token'];
    $uid = $result['uid'];
  }

} catch (Exception $e) {
  $res['ERR'] = ERR_USER_NOT_LOGGED;
  finish($res);

  print $e->getMessage();
}

/* Verify action */
$action = strtolower($action);
if (!in_array($action, ACTIONS)) {
  $res['ERR'] = ERR_ACTION_NOT_RECOGNIZED;
  finish($res);
}

/* Actions */
switch ($action) {

  /* ACTION NEW */
  // Requires input data:
  //   $data = {list_name: <LIST_NAME>, list_products: [<LIST_PRODUCTS>], users: [<USERS>]}
  // Returns:
  //   See: get_list_products(...)
  case ACTIONS[0]:
    if (!array_key_exists('list_name', $data) || !array_key_exists('list_products', $data) ) {
      $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
      finish($res);
    } else {
      $list_name = $data['list_name'];

      // create list
      $lid = -1;
      try {
        $sth = $dbh->prepare('insert into lists (listname) VALUES (:listname)');
        $sth->bindParam(':listname', $list_name, MAX_LIST_NAME);
        $sth->execute();

        $lid = intval($dbh->lastInsertId('lid'));

      } catch (Exception $e) {
        error_log($e->getMessage());
        $res['ERR'] = ERR_INPUT_DATA_MALFORMED;
        finish($res);
      }

      // update perm table for creator
      try {
        $sth = $dbh->prepare('insert into list_membership(lid,uid) VALUES (:lid,:uid)');
        $sth->bindParam(':lid', $lid);
        $sth->bindParam(':uid', $uid);
        $sth->execute();

      } catch (Exception $e) {
        error_log($e->getMessage());
        $res['ERR'] = ERR_DATABASE;
      }

      // update perm table for optional additional users
      if(array_key_exists('users', $data) && count($data['users']) > 0) {
        share_list_with_users($dbh, $lid, $data['users']);
      }

      // get all known pids
      $ids = get_create_pids_from_names($dbh, $data['list_products']);

      // insert all products into list
      try {
        $inQuery = implode(',', array_fill(0, count($ids), "($lid,?,?)"));
        $sth = $dbh->prepare('INSERT INTO list_elements(lid,pid,quantity) VALUES ' . $inQuery);
        for($i = 1, $j = 0; $i <= count($ids) * 2; $i += 2, $j++) {
          $product = $data['list_products'][$j];
          $sth->bindValue($i, $ids[$product['n']], PDO::PARAM_INT);
          $sth->bindValue($i + 1, $product['q'], PDO::PARAM_INT);
        }
        $sth->execute();

      } catch (Exception $e) {
        error_log($e->getMessage());
      }

      $res['JSON_DATA'] = get_list_products($dbh, $lid);
    }
    break;

  /* ACTION DEL */
  // Requires input data:
  //   $data = {list_id: <LIST_ID>}
  // Returns:
  //   See: {}
  case ACTIONS[1]:
    if (!array_key_exists('list_id', $data)) {
      $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
      finish($res);
    }
    $lid = intval($data['list_id']);

    try {
      $sth = $dbh->prepare('delete from list_membership where uid = :uid and lid = :lid');
      $sth->bindParam(':uid', $uid, PDO::PARAM_INT);
      $sth->bindParam(':lid', $lid, PDO::PARAM_INT);
      $sth->execute();

    } catch (Exception $e) {
      $res['ERR'] = ERR_INPUT_DATA_MALFORMED;
      finish($res);
    }
    break;

  /* ACTION MOD */
  // Requires input data:
  //   $data = {list_id: [<LIST_ID>], add: [<LIST_PRODUCTS>], del: [<LIST_PRODUCTS]}
  // Returns:
  //   See: get_list_products(...)
  case ACTIONS[2]:
    /* parameters check */
    if (!array_key_exists('list_id', $data) || !array_key_exists('add', $data) ||
      !array_key_exists('del', $data)) {

      $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
      finish($res);
    }
    $lid = intval($data['list_id']);

    /* perm check */
    if(!check_user_list_permission($dbh, $lid, $uid)) {
      $res['ERR'] = ERR_USER_NOT_AUTHORIZED;
      finish($res);
    }

    /* get lists */
    $products_to_add = $data['add'];
    $products_to_del = $data['del'];

    /* process lists */
    // ADD LIST
    if(count($products_to_add) > 0) {
      // get all known ids & create if product is new
      $ids_to_add = get_create_pids_from_names($dbh, $products_to_add);

      // insert values
      $dbh->beginTransaction();
      try {
        for($i=1,$j=0; $i<=count($ids_to_add) * 2; $i+=2, $j++) {
          $sth = $dbh->prepare("insert into list_elements(lid,pid,quantity) values ($lid,:pid,:q) ".
            "on duplicate key update quantity = quantity + :q");
          $product = $products_to_add[$j];

          $sth->bindParam(':pid', $ids_to_add[$product['n']], PDO::PARAM_INT);
          $sth->bindParam(':q', $product['q'], PDO::PARAM_INT);
          $sth->execute();
        }
        $dbh->commit();

      } catch (Exception $e) {
        error_log($e->getMessage());
        $dbh->rollBack();
        $res['ERR'] = ERR_DATABASE;

        finish($res);
      }
    }

    // DEL LIST
    if(count($products_to_del) > 0) {
      // get only known product ids (because we want to delete / decrease products)
      $product_names = [];
      foreach ($products_to_del as $product) {
        array_push($product_names, $product['n']);
      }
      $ids_to_del = get_products_id($dbh, $product_names);

      // delete values
      $dbh->beginTransaction();
      try {
        for ($i = 1; $i <= count($ids_to_del); $i++) {
          $sth = $dbh->prepare("update list_elements SET quantity = quantity - :q WHERE lid=$lid AND pid=:pid");
          $product = $products_to_del[$i - 1];

          $sth->bindParam(':pid', $ids_to_del[$product['n']], PDO::PARAM_INT);
          $sth->bindParam(':q', $product['q'], PDO::PARAM_INT);
          $sth->execute();

          // update history
          $sth = $dbh->prepare("insert into history(lid,uid,pid,quantity) VALUES (:lid,uid,:pid,:quantity)");
          $sth->bindParam(':pid', $ids_to_del[$product['n']], PDO::PARAM_INT);
          $sth->bindParam(':quantity', $product['q'], PDO::PARAM_INT);
          $sth->bindParam(':lid', $lid, PDO::PARAM_INT);
          $sth->bindParam(':uid', $uid, PDO::PARAM_INT);

          $sth->execute();
        }
        $dbh->commit();

      } catch (Exception $e) {
        $dbh->rollBack();
        error_log($e->getMessage());
        $res['ERR'] = ERR_DATABASE;

        finish($res);
      }

      // cleanup database from 0-valued elements
      try {
        $sth = $dbh->exec("DELETE FROM list_elements WHERE quantity<1");

      } catch (Exception $e) {
        error_log($e->getMessage());
        $res['ERR'] = ERR_DATABASE;
      }
    }

    $res['JSON_DATA'] = get_list_products($dbh, $lid);
    break;

  /* ACTION GET */
  // Requires input data:
  //   $data = {list_id: <LIST_ID>}
  // Returns:
  //   See: get_list_products(...)
  case ACTIONS[3]:
    if (!array_key_exists('list_id', $data)) {
      $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
      finish($res);
    }
    $lid = intval($data['list_id']);

    if(check_user_list_permission($dbh, $lid, $uid)) {
      $res['JSON_DATA'] = get_list_products($dbh, $lid);

    } else {
      $res['ERR'] = ERR_USER_NOT_AUTHORIZED;
      finish($res);
    }
    break;

  /* ACTION DIR */
  // Requires input data:
  //   $data = {}
  // Returns:
  //   $out  = [{id: <LIST_ID>, n: <LIST_NAME>}, ...]
  case ACTIONS[4]:
    try {
      $sth = $dbh->prepare("select lists.lid, lists.listname from lists, list_membership " .
        "where lists.lid = list_membership.lid and list_membership.uid = :uid");
      $sth->bindParam(':uid', $uid, PDO::PARAM_INT);
      $sth->execute();

      $result = $sth->fetchAll(PDO::FETCH_ASSOC);

      $lists = [];
      foreach ($result as $r) {
        $elem = array('id' => $r['lid'], 'n' => $r['listname']);
        array_push($lists, $elem);
      }

      $res['JSON_DATA'] = $lists;

    } catch (Exception $e) {
      die($e->getMessage());
    }
    break;

  /* ACTION SHARE */
  // Requires input data:
  //   $data = {list_id: <LIST_ID>, users: [<USERS>]}
  // Returns:
  //   See: get_list_products(...)
  case ACTIONS[5]:

    if (!array_key_exists('list_id', $data) || !array_key_exists('users', $data)) {
      $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
      finish($res);
    }
    $lid = intval($data['list_id']);
    $users = $data['users'];

    if(!check_user_list_permission($dbh, $lid, $uid)) {
      $res['ERR'] = ERR_USER_NOT_AUTHORIZED;
      finish($res);
    }

    share_list_with_users($dbh, $lid, $users);
    $res['JSON_DATA'] = get_list_products($dbh, $lid);
    break;

  /* ACTION PROCESS */
  // Requires input data:
  //   $data = {list_id: <LIST_ID>, img: <image_data>}
  // Returns:
  //   See: [{n: <PRODUCT_NAME>, q: <QUANTITY>}]
  case ACTIONS[6]:

    if (!array_key_exists('img', $data)) {
      $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
      finish($res);
    }
    $img_raw = $data['img'];

    // recognize image
    $res['JSON_DATA'] = get_json_from_image($img_raw);
    if($res['JSON_DATA'] == "") {
      $res['ERR'] = ERR_IMAGE_NOT_RECOGNIZED;
      finish($res);
    }

    break;

  /* ACTION STAT_LIST_USER */
  // Requires input data:
  //   $data = {list_id: <LIST_ID>}
  // Returns:
  //   $out  = {total: <TOTAL_COUNT>, contributors: [{user: <USERNAME>, count: <COUNT>}, ...]}
  case ACTIONS[7]:

    if (!array_key_exists('list_id', $data)) {
      $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
      finish($res);
    }
    $lid = intval($data['list_id']);

    if(!check_user_list_permission($dbh, $lid, $uid)) {
      $res['ERR'] = ERR_USER_NOT_AUTHORIZED;
      finish($res);
    }

    // get the user contribution stats
    $stats = ['total' => 0, 'contributors' => []];
    try {
      $sth = $dbh->prepare("select users.nickname as user,SUM(history.quantity) as q from users,history ".
        "where history.lid=:lid AND history.uid=users.uid GROUP BY history.uid");
      $sth->bindParam(':lid', $lid);
      $sth->execute();

      $result = $sth->fetchAll();

      foreach ($result as $r) {
        $stats['total'] += $r['q'];

        array_push($stats['contributors'], ['user' => $r['user'], 'count' => $r['q']]);
      }

    } catch (Exception $e) {
      error_log($e->getMessage());
      die;
    }

    $res['JSON_DATA'] = $stats;

    break;


  /* ACTION STAT_LIST_PRODUCT */
  // Requires input data:
  //   $data = {list_id: <LIST_ID>}
  // Returns:
  //   $out  = {total: <TOTAL_COUNT>, contributors: [{product: <PRODUCT>, count: <COUNT>}, ...]}
  case ACTIONS[8]:

    if (!array_key_exists('list_id', $data)) {
      $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
      finish($res);
    }
    $lid = intval($data['list_id']);

    if(!check_user_list_permission($dbh, $lid, $uid)) {
      $res['ERR'] = ERR_USER_NOT_AUTHORIZED;
      finish($res);
    }

    // get the user contribution stats
    $stats = ['total' => 0, 'contributors' => []];
    try {
      $sth = $dbh->prepare("select products.productname as product,SUM(history.quantity) as q " .
        "from products,history where history.lid=:lid AND history.pid=products.pid GROUP BY product");
      $sth->bindParam(':lid', $lid);
      $sth->execute();

      $result = $sth->fetchAll();

      foreach ($result as $r) {
        $stats['total'] += $r['q'];

        array_push($stats['contributors'], ['product' => $r['product'], 'count' => $r['q']]);
      }

    } catch (Exception $e) {
      error_log($e->getMessage());
      die;
    }

    $res['JSON_DATA'] = $stats;

    break;

  default:
    die("This action is available, but not supported yet. Contact the server admin for more info!");
    break;
}

finish($res);
