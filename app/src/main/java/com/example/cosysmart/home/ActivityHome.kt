package com.example.cosysmart.home
import com.example.cosysmart.R
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivityHome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val userRole = intent.getStringExtra("USER_ROLE") ?: "buyer"

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationViewHome)
        val txtTitle = findViewById<TextView>(R.id.txtHeaderTitle)
        val txtSubtitle = findViewById<TextView>(R.id.txtHeaderSubtitle)

        if (userRole == "vendor") {
            bottomNav.inflateMenu(R.menu.menu)
            txtSubtitle.text = "PANEL DE SOCIO VENDEDOR"
            txtTitle.text = "Gestiona tu negocio y catálogo."
            Toast.makeText(this, "Modo Vendedor Activo", Toast.LENGTH_SHORT).show()
        } else {

            bottomNav.inflateMenu(R.menu.menu)
            txtSubtitle.text = "TU HOGAR, A TU MANERA"
            txtTitle.text = "Encuentra tu lugar de bienestar."
            Toast.makeText(this, "Modo Comprador Activo", Toast.LENGTH_SHORT).show()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                else -> true
            }
        }
    }
}