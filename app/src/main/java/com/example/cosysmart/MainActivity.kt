package com.example.cosysmart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.cosysmart.register.RegisterActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val btnExplore = findViewById<Button>(R.id.btnExplore)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnExplore?.setOnClickListener {
            Toast.makeText(this, "¡Bienvenido a explorar CosySmart!", Toast.LENGTH_SHORT).show()
        }

        btnLogin?.setOnClickListener {
            Toast.makeText(this, "Ir a Iniciar Sesión", Toast.LENGTH_SHORT).show()
        }

        btnRegister?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}