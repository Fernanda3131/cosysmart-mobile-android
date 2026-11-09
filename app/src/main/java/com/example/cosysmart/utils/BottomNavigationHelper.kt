package com.example.cosysmart.utils

import android.app.Activity
import android.content.Intent
import com.example.cosysmart.R
import com.example.cosysmart.admin.ActivityHomeAdmin
import com.example.cosysmart.admin.AdminFinanceActivity
import com.example.cosysmart.admin.products.AdminCatalogActivity
import com.example.cosysmart.admin.users.ActivityAdminUsers
import com.example.cosysmart.home.ActivityHome
import com.example.cosysmart.seller.management.ActivitySellerOrders
import com.example.cosysmart.seller.management.ActivitySellerProducts
import com.example.cosysmart.seller.profile.ActivitySellerProfile
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

    fun setupBuyerNav(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
        bottomNavigationView.menu.clear()
        bottomNavigationView.inflateMenu(R.menu.menu_buyer)
        bottomNavigationView.selectedItemId = currentItemId

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.nav_buyer_inicio -> {
                    val intent = Intent(activity, ActivityHome::class.java).apply {
                        putExtra("USER_ROLE", "buyer")
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    activity.startActivity(intent)
                    activity.finish()
                    true
                }
                R.id.nav_buyer_catalogo -> {
                    true
                }
                R.id.nav_buyer_favoritos -> {
                    true
                }
                R.id.nav_buyer_perfil -> {
                    true
                }
                else -> false
            }
        }
    }

    fun setupSellerNav(activity: Activity, bottomNavigationView: BottomNavigationView, currentItemId: Int) {
        bottomNavigationView.menu.clear()
        bottomNavigationView.inflateMenu(R.menu.menu_seller)
        bottomNavigationView.selectedItemId = currentItemId

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.nav_seller_panel -> {
                    val intent = Intent(activity, ActivityHome::class.java).apply {
                        putExtra("USER_ROLE", "seller")
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    activity.startActivity(intent)
                    activity.finish()
                    true
                }
                R.id.nav_seller_productos -> {
                    if (activity !is ActivitySellerProducts) {
                        val intent = Intent(activity, ActivitySellerProducts::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        activity.startActivity(intent)
                        activity.finish()
                    }
                    true
                }
                R.id.nav_seller_pedidos -> {
                    if (activity !is ActivitySellerOrders) {
                        val intent = Intent(activity, ActivitySellerOrders::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        activity.startActivity(intent)
                        activity.finish()
                    }
                    true
                }
                R.id.nav_seller_perfil -> {
                    if (activity !is ActivitySellerProfile) {
                        val intent = Intent(activity, ActivitySellerProfile::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        activity.startActivity(intent)
                        activity.finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}