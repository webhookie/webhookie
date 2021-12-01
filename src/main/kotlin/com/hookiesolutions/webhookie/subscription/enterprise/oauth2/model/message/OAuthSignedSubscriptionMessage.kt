package com.hookiesolutions.webhookie.subscription.enterprise.oauth2.model.message

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.core.OAuth2AccessToken
import java.time.Duration

data class OAuthSignedSubscriptionMessage(
  override val originalMessage: ConsumerMessage,
  override val spanId: String,
  override val subscription: SubscriptionDTO,
  override val delay: Duration = Duration.ZERO,
  override val numberOfRetries: Int = 0,
  override val totalNumberOfTries: Int = 1,
  override val subscriptionIsBlocked: Boolean = subscription.isBlocked,
  override val subscriptionIsWorking: Boolean = !subscription.isBlocked,
  val token: String
): SignableSubscriptionMessage {
  override fun addMessageHeaders(headers: HttpHeaders) {
    super.addMessageHeaders(headers)
    headers.add(HttpHeaders.AUTHORIZATION, "${OAuth2AccessToken.TokenType.BEARER.value} $token")
  }
}
