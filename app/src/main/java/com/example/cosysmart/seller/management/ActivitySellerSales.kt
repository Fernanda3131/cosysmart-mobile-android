package com.example.cosysmart.seller.management

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivitySellerSales : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_sales)

        val btnBackToProducts = findViewById<ImageButton>(R.id.btnBackToProducts)
        btnBackToProducts.setOnClickListener {
            val intent = Intent(this, ActivitySellerProducts::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationViewSeller)
        BottomNavigationHelper.setupSellerNav(this, bottomNav, R.id.nav_seller_panel)
    }
}