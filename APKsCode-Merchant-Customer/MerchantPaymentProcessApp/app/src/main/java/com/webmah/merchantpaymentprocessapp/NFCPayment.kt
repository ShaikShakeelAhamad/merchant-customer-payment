package com.webmah.merchantpaymentprocessapp

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_nfcpayment.*
import java.text.SimpleDateFormat
import java.util.*

class NFCPayment : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfcpayment)

        val MERCHANTINFO = intent.getStringExtra("MERCHANTINFO")

        sendTOCustomer.setOnClickListener {

            val total_amount = billAmount.text.toString()
            val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
            val currentDateandTime: String = sdf.format(Date())
            val MERCHANTINFOBILLPAYMENT = "$MERCHANTINFO, $total_amount, $currentDateandTime"

            val intent = Intent(this, SendEncyptedData::class.java)
            intent.putExtra("MERCHANTINFOBILLPAYMENT", MERCHANTINFOBILLPAYMENT)
            startActivity(intent)
        }
    }

}