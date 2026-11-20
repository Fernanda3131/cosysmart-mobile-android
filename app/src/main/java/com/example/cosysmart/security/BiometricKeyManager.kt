
package com.example.cosysmart.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Locale

// Manager de llaves biométricas
object BiometricKeyManager {

    private const val KEY_ALIAS = "CosySmartBiometricKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun generarParDeLlaves(): Boolean {
        return try {

            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }

            if (keyStore.containsAlias(KEY_ALIAS)) {
                return keyStore.getCertificate(KEY_ALIAS)?.publicKey != null
            }

            val generator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                ANDROID_KEYSTORE
            )

            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN
            ).apply {

                setAlgorithmParameterSpec(
                    ECGenParameterSpec("secp256r1")
                )

                setDigests(KeyProperties.DIGEST_SHA256)

                setUserAuthenticationRequired(true)

                setUserAuthenticationValidityDurationSeconds(-1)

            }.build()

            generator.initialize(spec)
            generator.generateKeyPair()

            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun eliminarLlaves(): Boolean {
        return try {

            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }

            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }

            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun obtenerClavePublicaPem(): String? {
        return try {

            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }

            val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey
                ?: return null

            val base64 = Base64.encodeToString(
                publicKey.encoded,
                Base64.NO_WRAP
            )

            val lineas = base64.chunked(64).joinToString("\n")

            "-----BEGIN PUBLIC KEY-----\n$lineas\n-----END PUBLIC KEY-----"

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun prepararFirma(mensaje: ByteArray): Signature? {
        return try {

            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }

            val privateKey = keyStore.getKey(
                KEY_ALIAS,
                null
            ) as? PrivateKey ?: return null

            Signature.getInstance("SHA256withECDSA").apply {
                initSign(privateKey)
                update(mensaje)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun codificarFirma(firma: ByteArray): String {

        return Base64.encodeToString(
            firma,
            Base64.URL_SAFE or
                    Base64.NO_WRAP or
                    Base64.NO_PADDING
        )
    }

    fun decodificarNonce(nonce: String): ByteArray? {
        return try {

            Base64.decode(
                nonce,
                Base64.URL_SAFE or
                        Base64.NO_WRAP or
                        Base64.NO_PADDING
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun construirMensajeFirma(
        challengeId: String,
        idCredencial: String,
        nonce: ByteArray
    ): ByteArray {

        val prefijo = "COSYSMART|BIOMETRIA_LOGIN|V1"

        val challenge = challengeId.lowercase(Locale.ROOT)
        val credential = idCredencial.lowercase(Locale.ROOT)

        val encabezado = "$prefijo\n$challenge\n$credential\n"
            .toByteArray(Charsets.US_ASCII)

        return encabezado + nonce
    }
}
