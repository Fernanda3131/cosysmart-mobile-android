package com.example.cosysmart.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminFinanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_finance)
        val btnBack = findViewById<ImageButton>(R.id.btnBackFinance)
        btnBack?.setOnClickListener {
            val intent = Intent(this, ActivityHomeAdmin::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNav != null) {
            BottomNavigationHelper.setupAdminNav(this, bottomNav, R.id.nav_finanzas)
        }

        Toast.makeText(this, "Balance Financiero cargado", Toast.LENGTH_SHORT).show()
    }
}