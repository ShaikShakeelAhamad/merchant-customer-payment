<?php
//this code recive client AES(SignClentPri(ServerPublic))

// if you are doing ajax with application-json headers
if (empty($_POST)) {
    $_POST = json_decode(file_get_contents("php://input"), true) ? : [];
}
$FromMerchant = str_replace("\n", "", $_POST['FromMerchant']);

$filedata = file_get_contents('nfctext.txt');

$keysdata = trim($filedata);

echo $keysdata;

?>