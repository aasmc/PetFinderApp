package ru.aasmc.petfinderapp.common.utils

import android.annotation.TargetApi
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedFile
import java.io.File
import java.security.KeyStore
import java.util.HashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

class Encryption {
    companion object {
        private const val KEYSTORE_ALIAS = "PetSaveLoginKey"
        private const val PROVIDER = "AndroidKeyStore"

        @TargetApi(23)
        fun generateSecretKey() {

        }

        fun createLoginPassword(context: Context): ByteArray {
            return ByteArray(0)
        }

        fun decryptPassword(context: Context, password: ByteArray): ByteArray {
            return ByteArray(0)
        }

        @TargetApi(23)
        fun encryptFile(context: Context, file: File): EncryptedFile? {
            return null
        }

        fun encrypt(
            dataToEncrypt: ByteArray,
            password: CharArray
        ): HashMap<String, ByteArray> {
            val map = HashMap<String, ByteArray>()

            //TODO: Add custom encrypt code here

            return map
        }

        fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {

            var decrypted: ByteArray? = null

            //TODO: Add custom decrypt code here

            return decrypted
        }

        /**
         * Keystore version of encryption.
         */
        private fun keystoreEncrypt(dataToEncrypt: ByteArray): HashMap<String, ByteArray> {
            val map = hashMapOf<String, ByteArray>()
            try {
                // get the key
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)

                val secretKeyEntry =
                    keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
                val secretKey = secretKeyEntry.secretKey

                // encrypt data
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val ivBytes = cipher.iv
                val encryptedBytes = cipher.doFinal(dataToEncrypt)
                map["iv"] = ivBytes
                map["encrypted"] = encryptedBytes
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return map
        }

        private fun keystoreDecrypt(map: HashMap<String, ByteArray>): ByteArray? {
            var decrypted: ByteArray? = null
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)

                val secretKeyEntry =
                    keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
                val secretKey = secretKeyEntry.secretKey

                // extract info from map
                val encryptedBytes = map["encrypted"]
                val ivBytes = map["iv"]
                // decrypt data
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, ivBytes)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                decrypted = cipher.doFinal(encryptedBytes)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return decrypted
        }

        @TargetApi(23)
        fun keyStoreTest() {
            val keyGenerator =
                KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )

            val keyGenParameterSpec =
                KeyGenParameterSpec.Builder(
                    "MyKeyAlias",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    //.setUserAuthenticationRequired(true) // requires lock screen, invalidated if lock screen is disabled
                    //.setUserAuthenticationValidityDurationSeconds(120) // only available x seconds from password authentication. -1 requires finger print - every time
                    .setRandomizedEncryptionRequired(true) // different ciphertext for same plaintext on each call
                    .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()

            val map =
                keystoreEncrypt("My very sensitive string!".toByteArray(Charsets.UTF_8))
            val decryptedBytes = keystoreDecrypt(map)
            decryptedBytes?.let {
                val decryptedString = String(it, Charsets.UTF_8)
                Log.e("MyApp", "The decrypted string is: $decryptedString")
            }
        }
    }
}













