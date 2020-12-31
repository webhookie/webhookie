package com.hookiesolutions.webhookie.common.message.subscription

import org.springframework.http.HttpHeaders

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
}
