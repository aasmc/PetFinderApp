package ru.aasmc.petfinderapp.common.data.api

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.Security
import java.util.*

class ReportManager {
    private val serverAuthenticator = Authenticator()
    private var clientPublicKeyString = ""

    init {
        System.setProperty("com.sun.net.ssl.checkRevocation", "true")
        Security.setProperty("ocsp.enable", "true")

        //Options to further config OCSP
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          val ts = KeyStore.getInstance("AndroidKeyStore")
          ts.load(null)
          val kmf =  KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
          val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
          // init cert path checking for offered certs and revocation checks against CLRs
          val cpb = CertPathBuilder.getInstance("PKIX")
          val rc: PKIXRevocationChecker = cpb.revocationChecker as PKIXRevocationChecker
          rc.options = (EnumSet.of(
              PKIXRevocationChecker.Option.PREFER_CRLS, // use CLR over OCSP
              PKIXRevocationChecker.Option.ONLY_END_ENTITY,
              PKIXRevocationChecker.Option.NO_FALLBACK)) // no fall back to OCSP
          val pkixParams = PKIXBuilderParameters(ts, X509CertSelector())
          pkixParams.addCertPathChecker(rc)
          tmf.init( CertPathTrustManagerParameters(pkixParams) )
          val ctx = SSLContext.getInstance("TLS")
          ctx.init(kmf.keyManagers, tmf.trustManagers, null)
        }
         */
    }

    fun login(userIDString: String, publicKeyString: String): String {
        clientPublicKeyString = publicKeyString
        return serverAuthenticator.publicKey()
    }

    fun sendReport(report: Map<String, Any>, callback: (Map<String, Any>) -> Unit) {
        GlobalScope.launch(Dispatchers.Default) {
            delay(1000L)
            withContext(Dispatchers.Main) {
                var result: Map<String, Any> = mapOf("success" to false)
                if (report.isNotEmpty()) {
                    val applicationID = report["application_id"] as Int
                    val reportID = report["report_id"] as String
                    val reportString = report["report"] as String
                    val stringToVerify = "$applicationID+$reportID+$reportString"
                    val bytesToVerify = stringToVerify.toByteArray(Charsets.UTF_8)

                    val signature = report["signature"] as String
                    val signatureBytes = Base64.decode(signature, Base64.NO_WRAP)

                    // simulated server verification of the request
                    val success = serverAuthenticator.verify(
                        signatureBytes, bytesToVerify,
                        clientPublicKeyString
                    )
                    // if server verified the request
                    if (success) {
                        //Process data
                        val confirmationCode = UUID.randomUUID().toString()
                        val bytesToSign =
                            confirmationCode.toByteArray(Charsets.UTF_8) // 1
                        val signedData = serverAuthenticator.sign(bytesToSign) // 2
                        val requestSignature =
                            Base64.encodeToString(signedData, Base64.NO_WRAP) // 3

                        result = mapOf(
                            "success" to true,
                            "confirmation_code" to confirmationCode,
                            "signature" to requestSignature
                        )
                    }
                }
                callback(result)
            }
        }
    }
}













