package com.example.cosysmart.register

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.example.cosysmart.R

class SecurityPoliciesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_policies)

        val cardBack = findViewById<MaterialCardView>(R.id.cardBack)
        val btnAccept = findViewById<MaterialButton>(R.id.btnAcceptPolicies)

        cardBack.setOnClickListener {
            finish()
        }
        btnAccept.setOnClickListener {
            Toast.makeText(this, "Políticas aceptadas correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}