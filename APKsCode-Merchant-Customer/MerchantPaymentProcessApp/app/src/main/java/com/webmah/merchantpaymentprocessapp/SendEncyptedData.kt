package com.webmah.merchantpaymentprocessapp
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_sendencrypteddata.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection

class SendEncyptedData  : AppCompatActivity() {

    private lateinit var keyguardManager: KeyguardManager
    private lateinit var signatureResult: String
    private lateinit var enMessage: String
    private val TAG = "MCMP"
    private val iv = "123456789abcdefh".toByteArray()
    private lateinit var clientPrivateKey: PrivateKey
    private lateinit var clientPublicKey: PublicKey
    private lateinit var clientAES: SecretKey
    private lateinit var serverResponse: String
    private lateinit var ClientNFCData: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sendencrypteddata)

        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        //Check if lock screen has been set up. Just displaying a Toast here but it shouldn't allow the user to go forward.
        if (!keyguardManager.isDeviceSecure) {
            Toast.makeText(this, "Secure lock screen hasn't set up.", Toast.LENGTH_LONG).show()
        }

        val MERCHANTINFOBILLPAYMENT = intent.getStringExtra("MERCHANTINFOBILLPAYMENT")
        m_data.text = "Merchant Payment Data: $MERCHANTINFOBILLPAYMENT"

        //checkNetworkConnection()
        checkKeysExists()
        //showAuthenticationScreen()

        if (checkNetworkConnection()) {
            val jsonObject = JSONObject()
            jsonObject.accumulate("FromMerchant", "FromMerchant")
            lifecycleScope.launch {
                val result = httpPostNFCSim("https://webmah.com/customerpaymentapp/NFCPaymentSimulationSendData.php", jsonObject)

                System.out.println(ClientNFCData)
                var delimiter = "-------"
                val parts = ClientNFCData.split(delimiter)
                c_id.text = parts[0].toString()
                val endata = parts[2].toString()
                cdata_encrypted.text = "Encrypted Customer Payment Data:$endata"
                encryptAndSend()
            }
        }
        else
            Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show()

    }

    private fun checkNetworkConnection(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connMgr.activeNetworkInfo
        val isConnected: Boolean = if(networkInfo != null) networkInfo.isConnected() else false
        if (networkInfo != null && isConnected) {
            // show "Connected" & type of network "WIFI or MOBILE"
            howIsConnected.text = "Connected " + networkInfo.typeName
        } else {
            // show "Not Connected"
            howIsConnected.text = "Not Connected"
        }
        return isConnected
    }

    private fun checkKeysExists(): Boolean {
        val sharedPreference =  getSharedPreferences(SHAREDLOCATION, Context.MODE_PRIVATE)
        if(sharedPreference.contains("clientPrivateKey") && sharedPreference.contains("clientPublicKey") && sharedPreference.contains("clientAES")){
            // decode the base64 encoded string
            val seck = sharedPreference.getString("clientAES", "no")
            if(seck == "no")
            {
                return false
            }
            System.out.println(seck)
            val secKey: ByteArray = Base64.decode(seck, Base64.DEFAULT)
            clientAES = SecretKeySpec(secKey, 0, secKey.size, "AES")

            // decode the base64 encoded string
            val pukey: ByteArray = Base64.decode(sharedPreference.getString("clientPublicKey", "no"), Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(pukey)
            val keyFactory = KeyFactory.getInstance("EC")
            clientPublicKey = keyFactory.generatePublic(keySpec)

            val prkey: ByteArray = Base64.decode(sharedPreference.getString("clientPrivateKey", "no"), Base64.DEFAULT)
            val keySpec1 = PKCS8EncodedKeySpec(prkey)
            val keyFactory1 = KeyFactory.getInstance("EC")
            clientPrivateKey = keyFactory1.generatePrivate(keySpec1)

            return true
        }
        return false
    }

    private fun encryptAndSend() {

        val pdatam = m_data.text.toString()
        val cid = c_id.text.toString()
        val cndata = cdata_encrypted.text.toString()
        val pdata = "$pdatam,$cid-------$cndata"
        //encryption
        serverResponse = "no"
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val parameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, clientAES, parameterSpec)
        val bytes = cipher.doFinal(pdata.toByteArray())
        val clientENPdata = Base64.encodeToString(bytes, Base64.DEFAULT)
        mdata_encrypted.text = "Encrypted Merchant Payment Data: $clientENPdata"
        val ivs = Base64.encodeToString(iv, Base64.DEFAULT)

        if (checkNetworkConnection()) {
            val jsonObject = JSONObject()
            jsonObject.accumulate("iv", ivs)
            jsonObject.accumulate("merchantENPdata", clientENPdata)
            lifecycleScope.launch {
                val result = httpPost("https://webmah.com/merchantpaymentprocessapp/EncryptedDataToBankServer.php", jsonObject)

                if(serverResponse.contains("valid", ignoreCase = true))
                {
                    bank_msg.text = "Payment Completed Successfully. And Successful Message Sent to Customer."
                }
            }
        }
        else
            Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show()

    }

    @Throws(IOException::class, JSONException::class)
    private suspend fun httpPostNFCSim(myUrl: String, jsonObject: JSONObject): String {

        val result = withContext(Dispatchers.IO) {
            val url = URL(myUrl)
            // 1. create HttpURLConnection
            val conn = url.openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            // 2. build JSON object
            //val jsonObject = buidJsonObject()

            // 3. add JSON content to POST request body
            setPostRequestContent(conn, jsonObject)

            // 4. make POST request to the given URL
            conn.connect()

            // 5. return response message
            conn.responseMessage + ""

            if (conn.responseCode == HttpsURLConnection.HTTP_OK) {
                val stream = BufferedInputStream(conn.inputStream)
                ClientNFCData = readStream(inputStream = stream)
            } else {
                ClientNFCData = "Problem in Getting Server Response"
            }

        }
        return result.toString()
    }

    @Throws(IOException::class, JSONException::class)
    private suspend fun httpPost(myUrl: String, jsonObject: JSONObject): String {

        val result = withContext(Dispatchers.IO) {
            val url = URL(myUrl)
            // 1. create HttpURLConnection
            val conn = url.openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            // 2. build JSON object
            //val jsonObject = buidJsonObject()

            // 3. add JSON content to POST request body
            setPostRequestContent(conn, jsonObject)

            // 4. make POST request to the given URL
            conn.connect()

            // 5. return response message
            conn.responseMessage + ""

            if (conn.responseCode == HttpsURLConnection.HTTP_OK) {
                val stream = BufferedInputStream(conn.inputStream)
                serverResponse = readStream(inputStream = stream)
            } else {
                serverResponse = "Problem in Getting Server Response"
            }

        }
        return result.toString()
    }

    @Throws(IOException::class)
    private fun setPostRequestContent(conn: HttpURLConnection, jsonObject: JSONObject) {

        val os = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(jsonObject.toString())
        Log.i(TAG, jsonObject.toString())
        writer.flush()
        writer.close()
        os.close()
    }

    private fun readStream(inputStream: BufferedInputStream): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        bufferedReader.forEachLine { stringBuilder.append(it) }
        return stringBuilder.toString()
    }

    private fun showAuthenticationScreen() {
        //This will open a screen to enter the user credentials (fingerprint, pin, pattern). We can display a custom title and description
        val intent: Intent? = keyguardManager.createConfirmDeviceCredentialIntent("User Authentication",
            "To be able to use this Merchant Payment Process App we need to confirm your identity. Please enter your pin/pattern or scan your fingerprint")
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_FOR_CREDENTIALS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_FOR_CREDENTIALS) {
            if (resultCode == Activity.RESULT_OK) {
                encryptAndSend()
                bank_msg.text = "Payment Completed Successfully. And Successful Message Sent to Customer."
            } else {
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

private const val REQUEST_CODE_FOR_CREDENTIALS = 1
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val SHAREDLOCATION = "MERCHANTPAYMENTAPP"