package ru.aasmc.petfinderapp.common.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.Exception

class Encryption {
    companion object {
        private const val KEYSTORE_ALIAS = "PetSaveLoginKey"
        private const val PROVIDER = "AndroidKeyStore"

        /**
         * Gets the secret key, generated for the application.
         */
        private fun getSecretKey(): SecretKey {
            val keyStore = KeyStore.getInstance(PROVIDER)

            // before the keystore can be accessed, it must be loaded.
            keyStore.load(null)
            return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        }

        private fun getCipher(): Cipher {
            return Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_GCM + "/"
                        + KeyProperties.ENCRYPTION_PADDING_NONE
            )
        }

        @TargetApi(Build.VERSION_CODES.R)
        fun generateSecretKey() {
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                //
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                // Require a lock scree to be setup and ensure that the secret key
                // is locked until the user authenticates on the device.
                // Revokes the key when the user removes or changes the lock screen.
                .setUserAuthenticationRequired(true)
                // The key is available for 120 seconds after authentication.
                // after that the user will need to authenticate again using biometrics.
                .setUserAuthenticationParameters(120, KeyProperties.AUTH_BIOMETRIC_STRONG)
                .build()

            val keyGenerator = KeyGenerator.getInstance(
                //
                KeyProperties.KEY_ALGORITHM_AES, PROVIDER
            )
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

        fun createLoginPassword(context: Context): ByteArray {
            val cipher = getCipher()
            val secretKey = getSecretKey()
            val random = SecureRandom()
            val passwordBytes = ByteArray(256)
            random.nextBytes(passwordBytes)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val ivParameters =
                cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
            val iv = ivParameters.iv
            PreferencesHelper.saveIV(context, iv)
            return cipher.doFinal(passwordBytes)
        }

        fun decryptPassword(context: Context, password: ByteArray): ByteArray {
            val cipher = getCipher()
            val secretKey = getSecretKey()
            val iv = PreferencesHelper.iv(context)
            val ivParameters = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameters)
            return cipher.doFinal(password)
        }

        fun encryptFile(context: Context, file: File): EncryptedFile {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
        }

        fun encrypt(
            dataToEncrypt: ByteArray,
            password: CharArray
        ): HashMap<String, ByteArray> {
            val map = HashMap<String, ByteArray>()

            try {
                // 1
                // Random salt for next step
                val random = SecureRandom()
                val salt = ByteArray(256)
                random.nextBytes(salt)

                // 2
                // PBKDF2 - derive the key from the password, don't use password directly
                val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
                val secretKeyFactory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1")
                // generate the key as ByteArray
                val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
                // wrap the key ByteArray into a SecretKeySpec object
                // Now we have a secure key to be used for encryption of data
                val keySpec = SecretKeySpec(keyBytes, "AES")

                // 3
                // Create initialization vector for AES
                val ivRandom =
                    SecureRandom() // not caching previous seeded instance of SecureRandom
                val iv = ByteArray(16)
                ivRandom.nextBytes(iv)
                val ivSpec = IvParameterSpec(iv)

                // 4
                // Encrypt
                // AES/CBC chooses AES with cipher block chaining mode.
                // PKCS7Padding is a well-known standard for padding. Since we are working with
                // blocks, not all data will fit perfectly into the block size,
                // so we need to pad the remaining space. Blocks are 128 bits long and
                // AES adds padding before encryption.
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

                // this performs the actual encryption.
                val encrypted = cipher.doFinal(dataToEncrypt)

                // 5
                map["salt"] = salt
                map["iv"] = iv
                map["encrypted"] = encrypted

            } catch (e: Exception) {
                Log.e("MYAPP", "encryption exception", e)
            }

            return map
        }

        fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {

            var decrypted: ByteArray? = null

            try {
                // 1
                val salt = map["salt"]
                val iv = map["iv"]
                val encrypted = map["encrypted"]

                // 2
                // regenerate key from password
                val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
                val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
                val keySpec = SecretKeySpec(keyBytes, "AES")
                // 3
                // Decrypt
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                decrypted = cipher.doFinal(encrypted)

            } catch (e: Exception) {
                Log.e("MYAPP", "decryption exception", e)
            }

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













