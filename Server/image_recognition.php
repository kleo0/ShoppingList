<?php

function get_json_from_image($raw) {
  file_put_contents("/tmp/data.jpg", base64_decode($raw), LOCK_EX);

  // error, image not recognized
  $result = "";

  //$result = [["n" => "apple", "q"=>"1"],["n" => "banana", "q"=>"1"]];
  return $result;
}