package com.example.cosysmart.seller.management

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.google.android.material.button.MaterialButton

class ActivitySellerCreateProduct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_create_product)

        val btnBack = findViewById<ImageButton>(R.id.btnBackCreateProduct)
        btnBack.setOnClickListener {
            finish()
        }

        val etName = findViewById<EditText>(R.id.etProductName)
        val etPrice = findViewById<EditText>(R.id.etProductPrice)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveProduct)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val price = etPrice.text.toString().trim()

            if (name.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "¡Producto '$name' creado exitosamente!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}