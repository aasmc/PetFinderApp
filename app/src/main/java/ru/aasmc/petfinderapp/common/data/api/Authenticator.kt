package ru.aasmc.petfinderapp.common.data.api

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class Authenticator {

    private val publicKey: PublicKey
    private val privateKey: PrivateKey

    init {
        // Create a KeyPairGenerator for the Elliptic Curve (EC) type
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        keyPairGenerator.initialize(256)
        val keyPair = keyPairGenerator.genKeyPair()

        publicKey = keyPair.public
        privateKey = keyPair.private
    }

    fun sign(data: ByteArray): ByteArray {
        // get ECDSA instance using the recommended hash-type of SHA-512
        val signature = Signature.getInstance("SHA512withECDSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }


    fun verify(signature: ByteArray, data: ByteArray, publicKeyString: String): Boolean {
        // get ECDSA instance using the recommended hash-type of SHA-512
        val verifySignature = Signature.getInstance("SHA512withECDSA")
        // convert a Base64 public key string into a PublicKey object.
        val bytes = android.util.Base64.decode(publicKeyString, android.util.Base64.NO_WRAP)
        val publicKey =
            KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(bytes))
        // initialize Signature with the public key for verification
        verifySignature.initVerify(publicKey)
        verifySignature.update(data)
        return verifySignature.verify(signature)
    }

    fun publicKey(): String {
        return android.util.Base64.encodeToString(
            publicKey.encoded,
            android.util.Base64.NO_WRAP
        )
    }
}