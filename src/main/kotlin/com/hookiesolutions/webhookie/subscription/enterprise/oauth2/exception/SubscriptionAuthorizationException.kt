package com.hookiesolutions.webhookie.subscription.enterprise.oauth2.exception

import com.hookiesolutions.webhookie.common.message.publisher.ServerResponse
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.messaging.MessageHeaders

class SubscriptionAuthorizationException(
  override val message: String,
  val subscriptionMessage: SignableSubscriptionMessage,
  val headers: MessageHeaders
) : RuntimeException(message) {
  val response: ServerResponse = ServerResponse(
    HttpStatus.UNAUTHORIZED,
    message.toByteArray(),
    HttpHeaders()
  )
}
