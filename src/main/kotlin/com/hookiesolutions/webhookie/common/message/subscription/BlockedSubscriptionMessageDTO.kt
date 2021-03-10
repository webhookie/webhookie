package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.WebhookieMessage
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
data class BlockedSubscriptionMessageDTO(
  val id: String?,
  val spanId: String,
  val consumerMessage: ConsumerMessage,
  val subscription: SubscriptionDTO,
  val blockedDetails: StatusUpdate
): WebhookieMessage {
  val topic: String
    get() = consumerMessage.topic

  override val traceId: String
    get() = consumerMessage.traceId
}
