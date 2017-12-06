<?php

function get_json_from_image($raw) {
  file_put_contents("/tmp/data.jpg", base64_decode($raw), LOCK_EX);

  // error, image not recognized
  $result = "";
  $out = [];
  exec("python ocr.py", $out);

  if(count($out) > 0 && $out[0] != "") {
    $result = [];

    foreach ($out as $item) {
      $raw = explode(';', $item);

      array_push($result, ["n" => $raw[1], "q" => $raw[0]]);
    }

  }

  return $result;
}