package com.hookiesolutions.webhookie.common.message

import com.hookiesolutions.webhookie.subscription.domain.CallbackSecurity
import org.springframework.http.HttpMethod
import org.springframework.messaging.Message

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/12/20 17:42
 */
data class SubscriptionMessage(
  val subscriptionName: String,
  val topic: String,
  val callbackUrl: String,
  val httpMethod: HttpMethod,
  val callbackSecurity: CallbackSecurity,
  val message: Message<ByteArray>,
  val traceId: String,
  val contentType: String
)
