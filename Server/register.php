<?php
require_once "connect.php";
require_once "error.php";
require_once "consts.php";

$res = ['ERR' => ERR_OK];

if(!$db_ok) {
  $res['ERR'] = ERR_DATABASE;
  finish($res);
}

if(empty($_POST['nickname']) || empty($_POST['password']) || empty($_POST['public_name'])) {
  $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
  finish($res);
}

$nickname = $_POST['nickname'];
$password = $_POST['password'];
$public_name = $_POST['public_name'];

if(!isBetween(strlen($nickname), 1, MAX_LOGIN_LEN) ||
    !isBetween(strlen($password), MIN_PASS_LEN, MAX_PASS_LEN) ||
    !isBetween(strlen($public_name), 1, MAX_PUB_NAME_LEN)) {
  print $nickname . $password . $public_name;

  $res['ERR'] = ERR_UNMET_DEPENDENCIES;
  finish($res);
}
$password = password_hash($password, PASSWORD_DEFAULT);

try {
  $sth = $dbh->prepare('select nickname from users where nickname = :nickname');
  $sth->bindParam(':nickname', $nickname, PDO::PARAM_STR, 16);
  $sth->execute();

} catch (Exception $e) {
  die($e);
}

if($sth->rowCount() > 0) {
  $res['ERR'] = ERR_USER_EXISTS;
  finish($res);
}

try {
  $sth = $dbh->prepare('insert into users (nickname,password,public_name) ' .
    'values (:nickname,:password,:public_name)');
  $sth->bindParam(':nickname', $nickname, PDO::PARAM_STR, MAX_LOGIN_LEN);
  $sth->bindParam(':password', $password, PDO::PARAM_STR);
  $sth->bindParam(':public_name', $public_name, PDO::PARAM_STR, MAX_PUB_NAME_LEN);
  $sth->execute();

} catch (Exception $e) {
  die($e);
}

finish($res);