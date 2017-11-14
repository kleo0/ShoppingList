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
  $products = ["lid" => $lid, "users" => "[]"];
  // TODO
  try {
    $sth = $dbh->prepare('select distinct lid,pid,productname,quantity from list_elements,products where pid=:pid');
    $sth->bindParam(':lid', $lid, PDO::PARAM_INT);
    $sth->execute();

    $result = $sth->fetchAll(PDO::FETCH_ASSOC);
    foreach ($result as $r) {
      $elem = array('name' => $r['productname'], 'q' => $r['quantity']);
      array_push($products, $elem);
    }
    return json_encode($products);

  } catch (Exception $e) {
    die($e->getMessage());
  }
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

/* standard script */
if (!$db_ok) {
  $res['ERR'] = ERR_DATABASE;
  finish($res);
}

if (empty($_POST['token']) || empty($_POST['action']) || empty($_POST['data'])) {
  $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
  finish($res);
}

$token = $_POST['token'];
$action = $_POST['action'];
$uid = -123;

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

      // verify json FIXME
/*      $json_list_data = json_decode($data['list_products']);
      if ($json_list_data === null && json_last_error() !== JSON_ERROR_NONE) {
        $res['ERR'] = ERR_INVALID_PRODUCTS_DATA;
        finish($res);
      }
*/
      // create list
      try {
        $sth = $dbh->prepare('insert into lists (listname) VALUES (:listname)');
        $sth->bindParam(':listname', $list_name, MAX_LIST_NAME);
        $sth->execute();

        $lid = intval($dbh->lastInsertId('lid'));

      } catch (Exception $e) {
        $res['ERR'] = ERR_INPUT_DATA_MALFORMED;
        finish($res);
      }

      // update perm table
      try {
        $sth = $dbh->prepare('insert into list_membership(lid,uid) VALUES (:lid,:uid)');
        $sth->bindParam(':lid', $lid);
        $sth->bindParam(':uid', $uid);
        $sth->execute();

      } catch (Exception $e) {
        die($e->getMessage());
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
  //   $out  = {}
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
  //   $out  = {}
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

  /* dir */
/*  case ACTIONS[4]:
  TODO   try {
      $sth = $dbh->prepare("select distinct users.uid,users.nickname,list_membership.lid,lists.listname" .
        " from list_membership,users,lists where lists.lid = :lid");
      $sth->bindParam(':lid', $lid);
    } catch (Exception $e) {
      die($e->getMessage());
    }
    break;
*/

  // TODO action share
  default:
    die("This action is available, but not supported yet. Contact the server admin for more info!");
    break;
}

finish($res);