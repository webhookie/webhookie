package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.subscription.utils.CryptoUtils
import com.hookiesolutions.webhookie.subscription.utils.CryptoUtils.Companion.ALG
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionSignature
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Service
class SubscriptionSignor(
  private val timeMachine: TimeMachine
) {
  fun sign(subscription: Subscription, consumerMessage: ConsumerMessage, spanId: String): Mono<SubscriptionSignature> {
    val time = timeMachine.now().toString()
    return Mono.justOrEmpty(subscription.callback.security)
      .zipWhen { CryptoUtils.hmac(it.secret.secret, subscription, time, consumerMessage.traceId, spanId) }
      .map {
        SubscriptionSignature.Builder()
          .keyId(it.t1.secret.keyId)
          .algorithm(ALG)
          .traceId(consumerMessage.traceId)
          .spanId(spanId)
          .date(time)
          .signature(it.t2)
          .build()
      }
  }
}
