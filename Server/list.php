<?php

require_once "connect.php";
require_once "error.php";
require_once "consts.php";

const ACTIONS = ["new", "del", "mod", 'get', 'dir', 'share'];

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
      $elem = array('name' => $r['productname'], 'q' => $r['quantity']);
      array_push($products, $elem);
    }
    $data['products'] = json_encode($products);

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
    $data['users'] = json_encode($users);

  } catch (Exception $e) {
    die($e->getMessage());
  }

  return stripslashes(json_encode($data));
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

$data = json_decode($_POST['data'], true);
if ($data === null && json_last_error() !== JSON_ERROR_NONE) {
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
        die($e->getMessage());
      }

      // update perm table for optional additional users
      if(array_key_exists('users', $data) && count($data['users']) > 0) {
        share_list_with_users($dbh, $lid, $data['users']);
      }

      // add all values to the list
      // TODO
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

  /* mod */
  case ACTIONS[2]:
    // TODO
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
  //   $out  = {lists: [{id: <LIST_ID>, n: <LIST_NAME>}, ...]}
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

      $data = json_encode($lists);
      $data = json_encode(['lists' => $data]);

      $res['JSON_DATA'] = stripslashes($data);

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

  default:
    die("This action is available, but not supported yet. Contact the server admin for more info!");
    break;
}

finish($res);