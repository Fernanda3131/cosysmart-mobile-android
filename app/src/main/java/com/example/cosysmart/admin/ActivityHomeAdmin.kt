package com.example.cosysmart.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
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
    }
}