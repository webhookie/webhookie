package com.hookiesolutions.webhookie.subscription.utils

import com.hookiesolutions.webhookie.subscription.domain.Subscription
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
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

    fun hmac(secret: String, subscription: Subscription, time: String, traceId: String, spanId: String): Mono<String> {
      val signatureValue =
        "(request-target): ${subscription.requestTarget()}" +
            " date: $time" +
            " x-trace-id: $traceId" +
            " x-span-id: $spanId"
      return hmac(signatureValue, secret)
    }

    private fun hmac(value: String, key: String): Mono<String> {
      return try {
        val sha256Hmac = Mac.getInstance(ALG)
        val secretKey = SecretKeySpec(key.toByteArray(), ALG)
        sha256Hmac.init(secretKey)
        val sign = sha256Hmac.doFinal(value.toByteArray())

        Base64.getEncoder().encodeToString(sign)
          .toMono()
      } catch (e: Exception) {
        Mono.error(e)
      }
    }
  }
}