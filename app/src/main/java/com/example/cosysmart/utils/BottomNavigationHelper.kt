package com.example.cosysmart.utils

import android.app.Activity
import android.content.Intent
import com.example.cosysmart.R
import com.example.cosysmart.admin.ActivityHomeAdmin
import com.example.cosysmart.admin.users.ActivityAdminUsers
// Importa también las Activities de catálogo y ajustes cuando las tengas creadas:
// import com.example.cosysmart.admin.products.ActivityAdminProducts
// import com.example.cosysmart.admin.settings.ActivityAdminSettings
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavigationHelper {

    fun setupAdminNav(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
        bottomNavigationView.selectedItemId = currentItemId

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.nav_admin -> {
                    val intent = Intent(activity, ActivityHomeAdmin::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    activity.startActivity(intent)
                    activity.finish()
                    true
                }
                R.id.nav_usuarios -> {
                    val intent = Intent(activity, ActivityAdminUsers::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    activity.startActivity(intent)
                    activity.finish()
                    true
                }
                R.id.nav_catalogo -> {
                    true
                }
                R.id.nav_finanzas -> {
                    true
                }
                else -> false
            }
        }
    }
}