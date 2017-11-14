<?php

function finish($obj) {
  print json_encode($obj);
  die;
}

function isBetween($val, $min, $max) {
  return $val >= $min && $val <= $max;
}

const ERR_OK = 0;                        // ok

const ERR_DATABASE = 1;                  // error correlated with database
const ERR_UNMET_DEPENDENCIES = 2;        // login / password is too long / too short
const ERR_USER_EXISTS = 3;               // user exists in db
const ERR_NOT_ALL_VARS_ARE_SET = 4;      // not all necessary variables are set
const ERR_INVALID_CREDENTIALS = 5;       // invalid login or password
const ERR_USER_NOT_LOGGED = 6;
const ERR_ACTION_NOT_RECOGNIZED = 7;
const ERR_INVALID_PRODUCTS_DATA = 8;
const ERR_INPUT_DATA_MALFORMED = 9;