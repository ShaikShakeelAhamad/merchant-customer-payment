package com.webmah.merchantpaymentprocessapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_merchantinfo.*

class MerchantInfo: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merchantinfo)

        beginNFCPayment.setOnClickListener {
            val tname = "MerchantPayment"
            val mer_id = mer_id.text.toString()
            val mer_name = mer_name.text.toString()
            val maddress = maddress.text.toString()
            val MERCHANTINFO = "$tname, $mer_id, $mer_name, $maddress"

            val intent = Intent(this, NFCPayment::class.java)
            intent.putExtra("MERCHANTINFO", MERCHANTINFO)
            startActivity(intent)

        }

    }
}