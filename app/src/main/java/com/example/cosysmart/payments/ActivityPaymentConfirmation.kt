package com.example.cosysmart.payments

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.home.ActivityHome
import com.google.android.material.button.MaterialButton

class ActivityPaymentConfirmation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_confirmation)
        val btnViewOrder = findViewById<MaterialButton>(R.id.btnViewOrder)
        btnViewOrder.setOnClickListener {
            val intent = Intent(this, ActivityHome::class.java).apply {
                putExtra("USER_ROLE", "buyer")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        // Botón "Seguir comprando" (regresa al inicio del comprador)
        val btnKeepShopping = findViewById<MaterialButton>(R.id.btnKeepShopping)
        btnKeepShopping.setOnClickListener {
            val intent = Intent(this, ActivityHome::class.java).apply {
                putExtra("USER_ROLE", "buyer")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}