package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsuccessfulSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscription.Keys.Companion.KEY_BLOCKED_MESSAGE_COLLECTION_NAME
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 14/12/20 17:45
 */
@Document
@TypeAlias(KEY_BLOCKED_MESSAGE_COLLECTION_NAME)
@CompoundIndexes(CompoundIndex(name = "message_time", def = "{'blockedDetails.time' : 1}"))
data class BlockedSubscription(
  val originalMessage: ConsumerMessage,
  val spanId: String,
  val subscription: SubscriptionDTO,
  val blockedDetails: BlockedDetailsDTO
): AbstractEntity() {
  class Keys {
    companion object {
      const val KEY_BLOCKED_MESSAGE_COLLECTION_NAME = "blocked_subscription"
    }
  }

  companion object {
    fun from(message: UnsuccessfulSubscriptionMessage): BlockedSubscription {
      return BlockedSubscription(
        message.subscriptionMessage.originalMessage,
        message.subscriptionMessage.spanId,
        message.subscriptionMessage.subscription,
        BlockedDetailsDTO(message.reason, message.time)
      )
    }

    fun from(message: SubscriptionMessage, at: Instant): BlockedSubscription {
      return BlockedSubscription(
        message.originalMessage,
        message.spanId,
        message.subscription,
        BlockedDetailsDTO("New Message", at)
      )
    }
  }
}
