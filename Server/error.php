<?php

function finish($obj) {
  error_log(json_encode($obj));
  print((json_encode($obj)));
  die;
}

function isBetween($val, $min, $max) {
  return $val >= $min && $val <= $max;
}

const ERR_OK                     = 0;     // ok

const ERR_DATABASE               = 1;     // error correlated with database
const ERR_UNMET_DEPENDENCIES     = 2;     // login / password is too long / too short
const ERR_USER_EXISTS            = 3;     // user exists in db
const ERR_NOT_ALL_VARS_ARE_SET   = 4;     // not all necessary variables are set
const ERR_INVALID_CREDENTIALS    = 5;     // invalid login or password
const ERR_USER_NOT_LOGGED        = 6;     // user is not logged in / login token is expired -> login required
const ERR_ACTION_NOT_RECOGNIZED  = 7;     // selected action is not valid
const ERR_INVALID_PRODUCTS_DATA  = 8;     // not all required fields are set correctly
const ERR_INPUT_DATA_MALFORMED   = 9;     // input data is not in valid or expected format
const ERR_USER_NOT_AUTHORIZED    = 10;    // user is not added to list, so cannot use it
