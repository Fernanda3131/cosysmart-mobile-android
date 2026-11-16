
package com.example.cosysmart.utils

import android.net.Uri
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import java.io.File

// Selector de imagen de perfil
class SelectorImagenPerfil(
    private val activity: ComponentActivity,
    private val imageView: ImageView,
    private val onImagenSeleccionada: (Uri) -> Unit
) {

    private var uriCamara: Uri? = null

    private val selectorGaleria =
        activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                mostrarImagen(it)
            }
        }

    private val selectorCamara =
        activity.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { guardada ->

            val uri = uriCamara

            if (guardada && uri != null) {
                mostrarImagen(uri)
            } else {
                uriCamara = null
            }
        }

    fun mostrarOpciones() {

        val opciones = arrayOf(
            "Tomar fotografía",
            "Elegir de la galería"
        )

        AlertDialog.Builder(activity)
            .setTitle("Actualizar foto de perfil")
            .setItems(opciones) { _, posicion ->

                when (posicion) {
                    0 -> abrirCamara()
                    1 -> abrirGaleria()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirCamara() {

        try {

            val directorio = File(activity.cacheDir, "images")

            if (!directorio.exists()) {
                directorio.mkdirs()
            }

            val archivo = File.createTempFile(
                "perfil_",
                ".jpg",
                directorio
            )

            val uri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                archivo
            )

            uriCamara = uri

            selectorCamara.launch(uri)

        } catch (e: Exception) {

            e.printStackTrace()

            AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage("No fue posible abrir la cámara.")
                .setPositiveButton("Aceptar", null)
                .show()
        }
    }

    private fun abrirGaleria() {
        selectorGaleria.launch("image/*")
    }

    private fun mostrarImagen(uri: Uri) {
        imageView.setImageURI(uri)
        onImagenSeleccionada(uri)
    }
}
