package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.CryptoUtils
import com.hookiesolutions.webhookie.common.CryptoUtils.Companion.ALG
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionSignature
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import org.springframework.stereotype.Service


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Service
class SubscriptionSignor(
  private val timeMachine: TimeMachine
) {
  fun sign(subscription: Subscription, consumerMessage: ConsumerMessage, spanId: String): SubscriptionSignature? {
    return subscription.callbackSecurity
      ?.let {
        val time = timeMachine.now().toString()
        val signatureValue =
          "(request-target): ${subscription.httpMethod.name} ${subscription.callbackUrl}" +
          " date: $time" +
          " x-trace-id: ${consumerMessage.headers.traceId}" +
          " x-span-id: $spanId"

        val signature = CryptoUtils.hmac(signatureValue, it.secret.secret)
        SubscriptionSignature(
          it.secret.keyId,
          ALG,
          consumerMessage.headers.traceId,
          spanId,
          time,
          signature
        )
      }
  }
}
