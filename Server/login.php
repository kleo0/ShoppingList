<?php
require_once "connect.php";
require_once "error.php";
require_once "consts.php";

$res = [
  'ERR' => ERR_OK,
  'TOKEN' => ""
];

if(!$db_ok) {
  $res['ERR'] = ERR_DATABASE;
  finish($res);
}

if(empty($_POST['nickname']) || empty($_POST['password'])) {
  $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
  finish($res);
}

$nickname = $_POST['nickname'];
$password = $_POST['password'];

if(!isBetween(strlen($nickname), 1, MAX_LOGIN_LEN) ||
  !isBetween(strlen($password), MIN_PASS_LEN, MAX_PASS_LEN)) {
  print $nickname . $password . $public_name;

  $res['ERR'] = ERR_INVALID_CREDENTIALS;
  finish($res);
}

// verify login
$pass_hash = "";
$uid = -1;
try {
  $sth = $dbh->prepare('select uid,nickname,password from users where nickname = :nickname');
  $sth->bindParam(':nickname', $nickname, PDO::PARAM_STR, 16);
  $sth->execute();

} catch (Exception $e) {
  die($e->getMessage());
}

if($sth->rowCount() != 1) {
  $res['ERR'] = ERR_INVALID_CREDENTIALS;
  finish($res);

} else {
  $result = $sth->fetch(PDO::FETCH_ASSOC);
  $uid = intval($result['uid']);
  $pass_hash = $result['password'];
}

if(password_verify($password, $pass_hash)) {
  $token = base64_encode(random_bytes(255));
  $token = substr($token, 0, 255);

  try {
    $sth = $dbh->prepare('insert into logged_users(uid,token) values(:uid,:token)');
    $sth->bindParam(':uid', $uid, PDO::PARAM_INT);
    $sth->bindParam(':token', $token, PDO::PARAM_STR, 255);
    $sth->execute();

  } catch (Exception $e) {
    die($e->getMessage());
  }
  $res['TOKEN'] = $token;

} else {
  $res['ERR'] = ERR_INVALID_CREDENTIALS;
  finish($res);
}

finish($res);