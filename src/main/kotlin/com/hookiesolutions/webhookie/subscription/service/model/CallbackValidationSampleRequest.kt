package com.hookiesolutions.webhookie.subscription.service.model

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionSignature
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails
import com.hookiesolutions.webhookie.subscription.domain.Secret
import com.hookiesolutions.webhookie.subscription.utils.CryptoUtils
import org.bson.types.ObjectId
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import reactor.core.publisher.Mono
import java.time.Instant

data class CallbackValidationSampleRequest(
  val httpMethod: HttpMethod,
  val url: String,
  val payload: String,
  val headers: HttpHeaders,
  val secret: Secret? = null,
  val traceId: String? = null,
  val spanId: String? = null
) {

  val callback: CallbackDetails
    get() = CallbackDetails("id", "TEMP", httpMethod, url)

  fun addMessageHeaders(
    httpHeaders: HttpHeaders,
    time: Instant
  ) {
    val traceId = this.traceId?: ObjectId.get().toHexString()
    val spanId = this.spanId?: ObjectId.get().toHexString()
    Mono
      .create<Secret> { it.success(secret) }
      .zipWhen { CryptoUtils.hmac(it.secret, callback, time.toString(), traceId, spanId) }
      .map {
        SubscriptionSignature.Builder()
          .keyId(it.t1.keyId)
          .algorithm(CryptoUtils.ALG)
          .traceId(traceId)
          .spanId(spanId)
          .date(time)
          .signature(it.t2)
          .build()
      }
      .subscribe { signature ->
        signature.headers
          .forEach {
            httpHeaders.add(it.key, it.value)
          }
      }

    httpHeaders.addAll(headers)
  }

  class Builder {
    private lateinit var callbackDetails: CallbackDetails
    private lateinit var payload: String
    private lateinit var headers: HttpHeaders

    fun callbackDetails(callbackDetails: CallbackDetails) = apply { this.callbackDetails = callbackDetails }
    fun payload(payload: String) = apply { this.payload = payload }
    fun headers(headers: HttpHeaders) = apply { this.headers = headers }

    fun build(): CallbackValidationSampleRequest = CallbackValidationSampleRequest(
      callbackDetails.httpMethod,
      callbackDetails.url,
      payload,
      headers,
      callbackDetails.security?.secret
    )
  }
}
