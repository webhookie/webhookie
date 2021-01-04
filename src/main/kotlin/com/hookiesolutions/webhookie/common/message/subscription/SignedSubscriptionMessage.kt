package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import org.springframework.http.HttpHeaders
import java.time.Duration

data class SignedSubscriptionMessage(
  override val originalMessage: ConsumerMessage,
  override val spanId: String,
  override val subscription: SubscriptionDTO,
  override val delay: Duration = Duration.ZERO,
  override val numberOfRetries: Int = 0,
  override val subscriptionIsBlocked: Boolean = subscription.isBlocked,
  override val subscriptionIsWorking: Boolean = !subscription.isBlocked,
  val signature: SubscriptionSignature
): SignableSubscriptionMessage {

  override fun addMessageHeaders(headers: HttpHeaders) {
    super.addMessageHeaders(headers)
    addSignatureHeaders(headers)
  }

  private fun addSignatureHeaders(headers: HttpHeaders) {
    signature.headers
      .forEach {
        headers.add(it.key, it.value)
      }
  }
}