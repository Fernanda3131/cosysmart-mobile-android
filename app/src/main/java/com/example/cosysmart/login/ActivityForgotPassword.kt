package com.example.cosysmart.login

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.google.android.material.button.MaterialButton

class ActivityForgotPassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etCorreo = findViewById<EditText>(R.id.etCorreoRecuperacion)
        val btnEnviar = findViewById<MaterialButton>(R.id.btnEnviarRecuperacion)
        val tvVolver = findViewById<TextView>(R.id.tvVolverLogin)

        btnEnviar.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            if (correo.isEmpty()) {
                etCorreo.error = "Por favor ingresa tu correo"
                return@setOnClickListener
            }

            Toast.makeText(this, "Se han enviado las instrucciones a $correo", Toast.LENGTH_LONG).show()
            finish() // Regresa al login
        }

        tvVolver.setOnClickListener {
            finish()
        }
    }
}