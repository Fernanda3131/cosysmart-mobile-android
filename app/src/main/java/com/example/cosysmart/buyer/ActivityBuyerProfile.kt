
package com.example.cosysmart.buyer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.security.SesionManager
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivityBuyerProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_profile)

        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            Toast.makeText(
                this,
                "Sesión cerrada correctamente",
                Toast.LENGTH_SHORT
            ).show()

            SesionManager.cerrarSesion(this)
        }

        val bottomNav = findViewById<BottomNavigationView>(
            R.id.bottomNavigationViewBuyer
        )

        BottomNavigationHelper.setupBuyerNav(
            this,
            bottomNav,
            R.id.nav_buyer_perfil
        )
    }
}
