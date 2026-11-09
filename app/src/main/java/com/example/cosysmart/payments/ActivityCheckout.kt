package com.example.cosysmart.payments

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.buyer.ActivityCart
import com.google.android.material.button.MaterialButton

class ActivityCheckout : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val btnBackCheckout = findViewById<ImageButton>(R.id.btnBackCheckout)
        btnBackCheckout.setOnClickListener {
            val intent = Intent(this, ActivityCart::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        val btnConfirmPayment = findViewById<MaterialButton>(R.id.btnConfirmPayment)
        btnConfirmPayment.setOnClickListener {
            val intent = Intent(this, ActivityPaymentConfirmation::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}