package com.hookiesolutions.webhookie.subscription.config.auth

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionSignature
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacDetails
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacSecurityScheme
import com.hookiesolutions.webhookie.subscription.service.model.CallbackValidationSampleRequest
import com.hookiesolutions.webhookie.subscription.utils.CryptoUtils
import org.bson.types.ObjectId
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HmacAuthorizationHeaderProvider(
  private val timeMachine: TimeMachine
): AuthorizationHeaderProvider() {
  override fun accepts(request: CallbackValidationSampleRequest): Boolean {
    return CallbackSecurityScheme.isHmac(request.securityScheme)
  }

  override fun customHeaders(request: CallbackValidationSampleRequest, headers: HttpHeaders): Mono<HttpHeaders> {
    val traceId = request.traceId?: ObjectId.get().toHexString()
    val spanId = request.spanId?: ObjectId.get().toHexString()
    val secret = request.securityScheme as? HmacSecurityScheme
    val time = timeMachine.now()
    val callback = request.callback
    return Mono.create<HmacDetails> {
      it.success(secret?.details)
    }
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
      .map { signature ->
        signature.headers
          .forEach {
            headers.add(it.key, it.value)
          }

        headers
      }
  }
}
