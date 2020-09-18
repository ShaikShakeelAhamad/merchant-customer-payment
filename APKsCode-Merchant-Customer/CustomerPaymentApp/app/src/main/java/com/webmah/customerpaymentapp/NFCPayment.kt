package com.webmah.customerpaymentapp

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

        val CUSTOMERINFO = intent.getStringExtra("CUSTOMERINFO")
        val CUSTOMERID = intent.getStringExtra("CUSTOMERID")

        payNow.setOnClickListener {

            val mar_id = mar_id.text.toString()
            val mar_name = mar_name.text.toString()
            val total_amount = total_amount.text.toString()
            val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
            val currentDateandTime: String = sdf.format(Date())
            val CUSTOMERBILLPAYMENT = "$CUSTOMERINFO, $mar_id, $mar_name, $total_amount, $currentDateandTime"

            val intent = Intent(this, SendEncyptedData::class.java)
            intent.putExtra("CUSTOMERBILLPAYMENT", CUSTOMERBILLPAYMENT)
            intent.putExtra("CUSTOMERID", CUSTOMERID)
            startActivity(intent)
        }
    }

}