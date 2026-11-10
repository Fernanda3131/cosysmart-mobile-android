package com.example.cosysmart.buyer

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.google.android.material.button.MaterialButton

class ActivityProductDetail : AppCompatActivity() {

    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val tvQuantity = findViewById<TextView>(R.id.tvQuantity)
        val btnMinus = findViewById<TextView>(R.id.btnMinus)
        val btnPlus = findViewById<TextView>(R.id.btnPlus)
        val btnAddToCart = findViewById<MaterialButton>(R.id.btnAddToCart)
        val btnTopCart = findViewById<RelativeLayout>(R.id.btnTopCart)
        val btnFav = findViewById<ImageButton>(R.id.btnFav)

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
            }
        }

        btnPlus.setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
        }

        btnTopCart.setOnClickListener {
            val intent = Intent(this, ActivityCart::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        btnAddToCart.setOnClickListener {
            Toast.makeText(this, "Producto agregado al carrito ($quantity)", Toast.LENGTH_SHORT).show()
        }

        btnFav.setOnClickListener {
            Toast.makeText(this, "Guardado en favoritos", Toast.LENGTH_SHORT).show()
        }
    }
}