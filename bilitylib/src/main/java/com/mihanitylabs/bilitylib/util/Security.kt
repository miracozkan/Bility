package com.mihanitylabs.bilitylib.util

import android.text.TextUtils
import android.util.Base64
import android.util.Log
import java.io.IOException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec


object Security {
    private const val TAG = "IABUtil/Security"
    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

    @Throws(IOException::class)
    fun verifyPurchase(signedData: String, signature: String?, key: String) = if (TextUtils.isEmpty(signedData)
        || TextUtils.isEmpty(key)
        || TextUtils.isEmpty(signature)
    ) {
        Log.e(TAG, "Purchase verification failed: missing data.")
        false
    } else {
        val publicKey: PublicKey = generatePublicKey(key)
        verify(publicKey, signedData, signature)
    }

    @Throws(IOException::class)
    private fun generatePublicKey(key: String) = try {
        val decodedKey: ByteArray = Base64.decode(key, Base64.DEFAULT)
        val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
        keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
    } catch (e: NoSuchAlgorithmException) {
        // "RSA" is guaranteed to be available.
        throw RuntimeException(e)
    } catch (e: InvalidKeySpecException) {
        val msg = "Invalid key specification: $e"
        Log.e(TAG, msg)
        throw IOException(msg)
    }

    private fun verify(publicKey: PublicKey?, signedData: String, signature: String?): Boolean {
        val signatureBytes: ByteArray = try {
            Base64.decode(signature, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Base64 decoding failed.")
            return false
        }
        try {
            val signatureAlgorithm: Signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signatureAlgorithm.initVerify(publicKey)
            signatureAlgorithm.update(signedData.toByteArray())
            if (!signatureAlgorithm.verify(signatureBytes)) {
                Log.e(TAG, "Signature verification failed.")
                return false
            }
            return true
        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available.
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "Invalid key specification.")
        } catch (e: SignatureException) {
            Log.e(TAG, "Signature exception.")
        }
        return false
    }
}
