
package com.example.cosysmart.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.cosysmart.R
import com.example.cosysmart.admin.products.AdminCatalogActivity
import com.example.cosysmart.security.SesionManager
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

// Activity desarrollada para el panel administrativo Home

class ActivityHomeAdmin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_admin)

        val bottomNav = findViewById<BottomNavigationView>(
            R.id.bottomNavigationView
        )

        bottomNav?.let {
            BottomNavigationHelper.setupAdminNav(
                this,
                it,
                R.id.nav_admin
            )
        }

        val btnLogout = findViewById<ImageButton>(
            R.id.btnLogoutAdmin
        )

        btnLogout?.setOnClickListener {
            Toast.makeText(
                this,
                "Cerrando sesión...",
                Toast.LENGTH_SHORT
            ).show()

            SesionManager.cerrarSesion(this)
        }

        val cardFinancieros = findViewById<CardView>(
            R.id.cardReportesFinancieros
        )

        cardFinancieros?.setOnClickListener {
            val intent = Intent(
                this,
                AdminFinanceActivity::class.java
            )
            startActivity(intent)
        }

        // Abrir catálogo global
        val cardCatalogo = findViewById<CardView>(
            R.id.cardCatalogoGlobal
        )

        cardCatalogo?.setOnClickListener {
            val intent = Intent(
                this,
                AdminCatalogActivity::class.java
            )
            startActivity(intent)
        }

        Toast.makeText(
            this,
            "Panel Administrativo cargado",
            Toast.LENGTH_SHORT
        ).show()
    }
}