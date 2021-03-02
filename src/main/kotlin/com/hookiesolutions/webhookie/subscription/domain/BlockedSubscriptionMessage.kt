package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
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

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 14/12/20 17:45
 */
@Document(collection = "blocked_subscription_message")
@TypeAlias("blocked_subscription_message")
@CompoundIndexes(CompoundIndex(name = "message_time", def = "{'blockedDetails.time' : 1}"))
data class BlockedSubscriptionMessage(
  val spanId: String,
  val consumerMessage: ConsumerMessage,
  val subscription: SubscriptionDTO,
  val blockedDetails: StatusUpdate
): AbstractEntity() {
  fun traceId(): String = consumerMessage.traceId

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
}
