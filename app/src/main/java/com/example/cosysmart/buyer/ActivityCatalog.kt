package com.example.cosysmart.buyer

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivityCatalog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val btnTopCart = findViewById<RelativeLayout>(R.id.btnTopCart)
        btnTopCart.setOnClickListener {
            val intent = Intent(this, ActivityCart::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationViewBuyer)
        BottomNavigationHelper.setupBuyerNav(this, bottomNav, R.id.nav_buyer_catalogo)
    }
}