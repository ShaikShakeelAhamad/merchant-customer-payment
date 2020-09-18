package com.webmah.customerpaymentapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_customerinfo.*
import java.text.SimpleDateFormat
import java.util.*

class CustomerInfo: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customerinfo)

        beginNFCPayment.setOnClickListener {
            val tname = "CustomerPayment"
            val cid = cid.text.toString()
            val cname = cname.text.toString()
            val caddress = caddress.text.toString()
            val CUSTOMERINFO = "$tname, $cid, $cname, $caddress"

            val intent = Intent(this, NFCPayment::class.java)
            intent.putExtra("CUSTOMERINFO", CUSTOMERINFO)
            intent.putExtra("CUSTOMERID", cid)
            startActivity(intent)

        }

    }
}