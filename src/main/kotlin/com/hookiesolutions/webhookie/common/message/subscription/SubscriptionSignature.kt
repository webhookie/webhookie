package com.hookiesolutions.webhookie.common.message.subscription

import org.springframework.http.HttpHeaders
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
data class SubscriptionSignature(
  val keyId: String,
  val algorithm: String,
  val traceId: String,
  val spanId: String,
  val date: String,
  val signature: String
) {
  private val signatureHeader: String
    get() = "keyId=$keyId,algorithm=$algorithm,headers=(request-target) date x-trace-id x-span-id,signature=$signature"

  val headers: Map<String, String>
    get() = mapOf(
      "Date" to date,
      "x-trace-id" to traceId,
      "x-span-id" to spanId,
      HttpHeaders.AUTHORIZATION to "Signature $signatureHeader"
    )

  class Builder {
    private lateinit var keyId: String
    private lateinit var algorithm: String
    private lateinit var traceId: String
    private lateinit var spanId: String
    private lateinit var date: String
    private lateinit var signature: String

    fun keyId(keyId: String) = apply { this.keyId = keyId }
    fun algorithm(algorithm: String) = apply { this.algorithm = algorithm }
    fun traceId(traceId: String) = apply { this.traceId = traceId }
    fun spanId(spanId: String) = apply { this.spanId = spanId }
    fun date(date: String) = apply { this.date = date }
    fun date(instant: Instant) = apply { this.date = instant.toString() }
    fun signature(signature: String) = apply { this.signature = signature }

    fun build() = SubscriptionSignature(keyId, algorithm, traceId, spanId, date, signature)
  }
}
