package com.example.cosysmart.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.cosysmart.R
import com.example.cosysmart.admin.products.AdminCatalogActivity
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivityHomeAdmin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_admin)


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNav != null) {
            BottomNavigationHelper.setupAdminNav(this, bottomNav, R.id.nav_admin)
        }

        val cardFinancieros = findViewById<CardView>(R.id.cardReportesFinancieros)
        cardFinancieros?.setOnClickListener {
            val intent = Intent(this, AdminFinanceActivity::class.java)
            startActivity(intent)
        }

        val cardCatalogo = findViewById<CardView>(R.id.cardCatalogoGlobal)
        cardCatalogo?.setOnClickListener {
            val intent = Intent(this, AdminCatalogActivity::class.java)
            startActivity(intent)
        }

        Toast.makeText(this, "Panel Administrativo cargado", Toast.LENGTH_SHORT).show()
    }
}