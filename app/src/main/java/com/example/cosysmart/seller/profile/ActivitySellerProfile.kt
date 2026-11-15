
package com.example.cosysmart.seller.profile

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.security.SesionManager
import com.example.cosysmart.seller.management.ActivitySellerSales
import com.example.cosysmart.seller.management.ActivitySellerOrders
import com.example.cosysmart.seller.management.ActivitySellerProducts
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivitySellerProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_profile)

        // Reportes
        val tvVerReporte = findViewById<TextView>(R.id.tvVerReporte)
        tvVerReporte.setOnClickListener {
            startActivity(Intent(this, ActivitySellerSales::class.java))
        }

        // Pedidos
        val tvVerTodoPedidos = findViewById<TextView>(R.id.tvVerTodoPedidos)
        tvVerTodoPedidos.setOnClickListener {
            startActivity(Intent(this, ActivitySellerOrders::class.java))
        }

        // Gestión de productos
        val btnGestionProductos =
            findViewById<LinearLayout>(R.id.btnGestionProductos)

        btnGestionProductos.setOnClickListener {
            startActivity(Intent(this, ActivitySellerProducts::class.java))
        }

        // Gestión de pedidos
        val btnGestionPedidos =
            findViewById<LinearLayout>(R.id.btnGestionPedidos)

        btnGestionPedidos.setOnClickListener {
            startActivity(Intent(this, ActivitySellerOrders::class.java))
        }

        // Cerrar sesión
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            Toast.makeText(
                this,
                "Cerrando sesión...",
                Toast.LENGTH_SHORT
            ).show()

            SesionManager.cerrarSesion(this)
        }

        // Navegación inferior
        val bottomNav =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationViewSeller
            )

        BottomNavigationHelper.setupSellerNav(
            this,
            bottomNav,
            R.id.nav_seller_panel
        )
    }
}
