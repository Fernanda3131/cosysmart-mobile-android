package com.example.cosysmart.utils

import android.app.Activity
import android.content.Intent
import com.example.cosysmart.R
import com.example.cosysmart.admin.ActivityHomeAdmin
import com.example.cosysmart.admin.products.AdminCatalogActivity // <--- Importamos nuestra nueva actividad de catálogo
import com.example.cosysmart.admin.AdminFinanceActivity
import com.example.cosysmart.admin.users.ActivityAdminUsers
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
                    val intent = Intent(activity, AdminCatalogActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    activity.startActivity(intent)
                    activity.finish()
                    true
                }
                R.id.nav_finanzas -> {
                    val intent = Intent(activity, AdminFinanceActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    activity.startActivity(intent)
                    activity.finish()
                    true
                }
                else -> false
            }
        }
    }
}