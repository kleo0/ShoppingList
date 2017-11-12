<?php

const ACTIONS = ["new", "del", "mod", 'get'];

$res = [
    'ERR' => ERR_OK,
    'LID' => -1,
    'LIST' => ""
];

if(!$db_ok) {
    $res['ERR'] = ERR_DATABASE;
    finish($res);
}

if(empty($_POST['token']) || empty($_POST['action']) || empty($_POST['products'])) {
    $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
    finish($res);
}

$token = $_POST['token'];
$action = $_POST['action'];
$uid = -123;
$products = $_POST['products'];

/* Login & get uid, trusted token */
try {
    $sth = $dbh->prepare("select uid,token from logged_users where token = :token");
    $sth->bindParam(':token', $token, PDO::PARAM_STR);
    $sth->execute();

    if($sth->rowCount() != 1) {
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
$lid = -1;
switch($action) {
    /* new */
    case ACTIONS[0]:
        $list_name = "";
        if(empty($_POST['lid']) || empty($_POST['name'])) {
            $res['ERR'] = ERR_NOT_ALL_VARS_ARE_SET;
            finish($res);
        } else {
            $lid = $_POST['lid'];
            $list_name = $_POST['name'];

            // verify json
            $json_list_data = json_decode($products);
            if($json_list_data === null && json_last_error() !== JSON_ERROR_NONE) {
                $res['ERR'] = ERR_INVALID_PRODUCTS_DATA;
                finish($res);
            }

            // create list
            try {
                $sth = $dbh->prepare('insert into lists (listname) VALUES (:listname)');
                $sth->bindParam(':listname', $list_name, MAX_LIST_NAME);
                $sth->execute();

                $lid = intval($dbh->lastInsertId('lid')); // TODO verify if it is valid

            } catch (Exception $e) {
                die($e->getMessage());
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
        }
        break;

    /* del */
    case ACTIONS[1]:
        // Delete only ownership, trigger should handle situation when no-one is using list
        // TODO add trigger to db
        try {
            $sth = $dbh->prepare('delete from list_membership where uid = :uid');
            $sth->bindParam(':uid', $uid, PDO::PARAM_INT);
            $sth->execute();

        } catch (Exception $e) {
            die($e->getMessage());
        }
        break;

    /* mod */
    case ACTIONS[2]:
        // TODO
        break;

    /* get */
    case ACTIONS[3]:
        // Nothing. Intentionally.
        break;

    default:
        die("This action is available, but not supported yet. Contact the server admin for more info!");
        break;
}

/* Get all list products & parse to JSON */
$products = [];
try {
    $sth = $dbh->prepare('select distinct lid,pid,productname,quantity from list_elements,products where pid=:pid');
    $sth->bindParam(':lid', $lid, PDO::PARAM_INT);
    $sth->execute();

    $result = $sth->fetchAll(PDO::FETCH_ASSOC);
    foreach ($result as $r) {
        $elem = array('name' => $r['productname'], 'q' => $r['quantity']);
        array_push($products, $elem);
    }
    $res['LIST'] = json_encode($result);

} catch (Exception $e) {
    die($e->getMessage());
}

finish($res);