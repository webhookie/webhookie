package com.hookiesolutions.webhookie.common

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
class CryptoUtils {
  companion object {
    const val ALG = "HmacSHA256"

    fun hmac(value: String, key: String): String {
      val sha256Hmac = Mac.getInstance(ALG)
      val secretKey = SecretKeySpec(key.toByteArray(), ALG)
      sha256Hmac.init(secretKey)
      val sign = sha256Hmac.doFinal(value.toByteArray())

      return Base64.getEncoder().encodeToString(sign)
    }
  }
}