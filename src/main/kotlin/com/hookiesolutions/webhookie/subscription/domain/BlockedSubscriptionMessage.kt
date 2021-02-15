package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.WebhookieHeaders
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage.Keys.Companion.KEY_SUBSCRIPTION
import org.bson.types.ObjectId
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.messaging.support.GenericMessage

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 14/12/20 17:45
 */
@Document(collection = "blocked_subscription_message")
@TypeAlias("blocked_subscription_message")
@CompoundIndexes(CompoundIndex(name = "message_time", def = "{'blockedDetails.time' : 1}"))
data class BlockedSubscriptionMessage(
  val headers: WebhookieHeaders,
  val originalSpanId: String,
  val payload: ByteArray,
  val messageHeaders: Map<String, Any>,
  val subscription: SubscriptionDTO,
  val blockedDetails: StatusUpdate
): AbstractEntity() {
  val originalMessage: ConsumerMessage
    get() = ConsumerMessage(
      headers,
      GenericMessage(payload, messageHeaders)
    )

  class Queries {
    companion object {
      fun bySubscriptionId(id: String): Criteria {
        return where("$KEY_SUBSCRIPTION.$UNDERSCORE_ID").`is`(ObjectId(id))
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_SUBSCRIPTION = "subscription"
    }
  }

  @Suppress("DuplicatedCode")
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BlockedSubscriptionMessage) return false

    if (headers != other.headers) return false
    if (originalSpanId != other.originalSpanId) return false
    if (!payload.contentEquals(other.payload)) return false
    if (messageHeaders != other.messageHeaders) return false
    if (subscription != other.subscription) return false
    if (blockedDetails != other.blockedDetails) return false

    return true
  }

  override fun hashCode(): Int {
    var result = headers.hashCode()
    result = 31 * result + originalSpanId.hashCode()
    result = 31 * result + payload.contentHashCode()
    result = 31 * result + messageHeaders.hashCode()
    result = 31 * result + subscription.hashCode()
    result = 31 * result + blockedDetails.hashCode()
    return result
  }
}
