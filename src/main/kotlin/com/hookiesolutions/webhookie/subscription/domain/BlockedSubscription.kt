package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsuccessfulSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscription.Keys.Companion.KEY_BLOCKED_MESSAGE_COLLECTION_NAME
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscription.Keys.Companion.KEY_SUBSCRIPTION
import org.bson.types.ObjectId
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.messaging.support.GenericMessage
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
  val topic: String,
  val traceId: String,
  val contentType: String,
  val authorizedSubscribers: Set<String> = emptySet(),
  val originalSpanId: String,
  val payload: ByteArray,
  val headers: Map<String, Any>,
  val subscription: SubscriptionDTO,
  val blockedDetails: BlockedDetailsDTO
): AbstractEntity() {
  fun subscriptionMessage(spanId: String): SubscriptionMessage {
    val msg = GenericMessage<ByteArray>(payload, headers)
    val originalMessage = ConsumerMessage(
      topic,
      traceId,
      contentType,
      authorizedSubscribers,
      msg
    )
    return SubscriptionMessage(originalMessage, spanId, subscription)
  }

  class Keys {
    companion object {
      const val KEY_BLOCKED_MESSAGE_COLLECTION_NAME = "blocked_subscription"
      const val KEY_SUBSCRIPTION = "subscription"
    }
  }

  class Queries {
    companion object {
      fun bySubscriptionId(id: String): Criteria {
        return where("$KEY_SUBSCRIPTION.$UNDERSCORE_ID").`is`(ObjectId(id))
      }
    }
  }

  companion object {
    fun from(message: UnsuccessfulSubscriptionMessage): BlockedSubscription {
      val originalMessage = message.subscriptionMessage.originalMessage
      return BlockedSubscription(
        originalMessage.topic,
        originalMessage.traceId,
        originalMessage.contentType,
        originalMessage.authorizedSubscribers,
        message.subscriptionMessage.spanId,
        originalMessage.payload,
        originalMessage.headers,
        message.subscriptionMessage.subscription,
        BlockedDetailsDTO(message.reason, message.time)
      )
    }

    fun from(message: SubscriptionMessage, at: Instant): BlockedSubscription {
      val originalMessage = message.originalMessage
      return BlockedSubscription(
        originalMessage.topic,
        originalMessage.traceId,
        originalMessage.contentType,
        originalMessage.authorizedSubscribers,
        message.spanId,
        originalMessage.payload,
        originalMessage.headers,
        message.subscription,
        BlockedDetailsDTO("New Message", at)
      )
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BlockedSubscription) return false

    if (topic != other.topic) return false
    if (traceId != other.traceId) return false
    if (contentType != other.contentType) return false
    if (authorizedSubscribers != other.authorizedSubscribers) return false
    if (originalSpanId != other.originalSpanId) return false
    if (!payload.contentEquals(other.payload)) return false
    if (headers != other.headers) return false
    if (subscription != other.subscription) return false
    if (blockedDetails != other.blockedDetails) return false

    return true
  }

  override fun hashCode(): Int {
    var result = topic.hashCode()
    result = 31 * result + traceId.hashCode()
    result = 31 * result + contentType.hashCode()
    result = 31 * result + authorizedSubscribers.hashCode()
    result = 31 * result + originalSpanId.hashCode()
    result = 31 * result + payload.contentHashCode()
    result = 31 * result + headers.hashCode()
    result = 31 * result + subscription.hashCode()
    result = 31 * result + blockedDetails.hashCode()
    return result
  }
}
