<?php
//this code recive client and Merchant AES [ MerchantAES(MerchnatInfo+ClientAES) ]

// if you are doing ajax with application-json headers
if (empty($_POST)) {
    $_POST = json_decode(file_get_contents("php://input"), true) ? : [];
}

$merchantiv = str_replace("\n", "", $_POST['iv']);
$merchantendata = str_replace("\n", "", $_POST['merchantENPdata']);

$fileclient = file_get_contents('../customerpaymentapp/nfctext.txt');

$clientdata = trim($fileclient);

$clientarray = (explode("-------",$clientdata));
$clientiv = $clientarray[1];
$clientendata = $clientarray[2];

$scfile = file_get_contents('../customerpaymentapp/serverkeys.txt');

$sdata = trim($scfile);
$sarray = (explode("-------",$sdata));
$serveraeskeyc = $sarray[2];

$smfile = file_get_contents('serverkeys.txt');

$smdata = trim($smfile);
$smarray = (explode("-------",$smdata));
$serveraeskeym = $smarray[2];

//$cmd = "java -jar ClientAuthAtServer.jar ".$input;

//$output = shell_exec($cmd); 

$output = "valid";

if(trim($output) == "valid"){
	echo "valid";
}
else{
	echo "fail";
}

?>