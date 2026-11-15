
package com.example.cosysmart.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.cosysmart.login.LoginActivity

object SesionManager {

    fun cerrarSesion(activity: Activity) {

        val preferences = activity.getSharedPreferences(
            "CosyPrefs",
            Context.MODE_PRIVATE
        )

        // Eliminar únicamente el token de autenticación.
        // Las credenciales biométricas se conservan.
        preferences.edit()
            .remove("JWT_TOKEN")
            .apply()

        // Regresar al Login y eliminar las pantallas anteriores.
        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        activity.startActivity(intent)
        activity.finish()
    }
}
