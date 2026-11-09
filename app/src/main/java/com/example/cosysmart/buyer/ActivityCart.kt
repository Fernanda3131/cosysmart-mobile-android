package com.example.cosysmart.buyer

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.home.ActivityHome
import com.example.cosysmart.payments.ActivityCheckout
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivityCart : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        val btnCloseCart = findViewById<ImageButton>(R.id.btnCloseCart)
        btnCloseCart.setOnClickListener {
            val intent = Intent(this, ActivityHome::class.java).apply {
                putExtra("USER_ROLE", "buyer")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        val btnCheckout = findViewById<MaterialButton>(R.id.btnCheckout)
        btnCheckout.setOnClickListener {
            val intent = Intent(this, ActivityCheckout::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationViewBuyer)
        BottomNavigationHelper.setupBuyerNav(this, bottomNav, R.id.nav_buyer_carrito)
    }
}