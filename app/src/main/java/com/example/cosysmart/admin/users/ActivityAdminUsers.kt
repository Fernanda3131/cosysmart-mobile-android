package com.example.cosysmart.admin.users

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cosysmart.R
import com.example.cosysmart.network.UsuarioItem
import com.example.cosysmart.admin.ActivityHomeAdmin
import com.example.cosysmart.utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class ActivityAdminUsers : AppCompatActivity() {

    private lateinit var adapter: UsuariosAdapter
    private var rolActual: String = "TODOS"
    private var textoActual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)

        // 1. Configurar barra de navegación inferior
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        BottomNavigationHelper.setupAdminNav(this, bottomNav, R.id.nav_usuarios)

        // 2. Botón de retroceso
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, ActivityHomeAdmin::class.java)
            startActivity(intent)
            finish()
        }

        // 3. Configurar RecyclerView
        val recycler = findViewById<RecyclerView>(R.id.recyclerUsuarios)
        recycler.layoutManager = LinearLayoutManager(this)

        val listaSimulada = listOf(
            UsuarioItem("Andrés Guzmán", "andres.guzman@cosysmart.com", "VENDEDOR", "Activo"),
            UsuarioItem("María Fernanda León", "maria.leon@gmail.com", "COMPRADOR", "Activo"),
            UsuarioItem("Carlos Pérez", "carlos.perez@cosysmart.com", "VENDEDOR", "Activo")
        )

        adapter = UsuariosAdapter(listaSimulada)
        recycler.adapter = adapter

        // 4. Configurar el Buscador
        val etBuscador = findViewById<EditText>(R.id.etBuscarUsuario)
        etBuscador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textoActual = s.toString()
                adapter.filtrar(textoActual, rolActual)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 5. Configurar Botones de Filtro por Rol
        val btnTodos = findViewById<MaterialButton>(R.id.btnFiltroTodos)
        val btnComprador = findViewById<MaterialButton>(R.id.btnFiltroComprador)
        val btnVendedor = findViewById<MaterialButton>(R.id.btnFiltroVendedor)

        btnTodos.setOnClickListener {
            rolActual = "TODOS"
            adapter.filtrar(textoActual, rolActual)
        }

        btnComprador.setOnClickListener {
            rolActual = "COMPRADOR"
            adapter.filtrar(textoActual, rolActual)
        }

        btnVendedor.setOnClickListener {
            rolActual = "VENDEDOR"
            adapter.filtrar(textoActual, rolActual)
        }
    }
}