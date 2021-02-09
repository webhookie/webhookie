package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractDocument.Keys.Companion.KEY_VERSION
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.subscription.domain.ApplicationDetails.Keys.Companion.KEY_APPLICATION_ID
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails.Keys.Companion.KEY_CALLBACK_ID
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_APPLICATION
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_BLOCK_DETAILS
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_CALLBACK
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_ENTITY
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_TOPIC
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.SUBSCRIPTION_COLLECTION_NAME
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Update

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:27
 */
@Document(collection = SUBSCRIPTION_COLLECTION_NAME)
@TypeAlias("subscription")
@CompoundIndexes(
  CompoundIndex(
    name = "subscription",
    def = "{'callback.callbackId' : 1, 'topic': 1}",
    unique = true
  )
)
data class Subscription(
  @Indexed
  val topic: String,
  val application: ApplicationDetails,
  val callback: CallbackDetails,
  val blockedDetails: BlockedDetailsDTO? = null
) : AbstractEntity() {
  fun dto(): SubscriptionDTO {
    return SubscriptionDTO(
      id!!,
      application,
      topic,
      callback.dto(),
      blockedDetails
    )
  }

  fun requestTarget(): String {
    return callback.requestTarget()
  }

  class Queries {
    companion object {
      fun topicIs(topic: String): Criteria {
        return where(KEY_TOPIC).`is`(topic)
      }

      fun isAuthorized(entities: Set<String>): Criteria {
        return where(KEY_ENTITY)
          .`in`(entities)
      }

      fun callbackIdIs(id: String): Criteria {
        return where("$KEY_CALLBACK.$KEY_CALLBACK_ID").`is`(id)
      }

      fun applicationIdIs(id: String): Criteria {
        return where("$KEY_APPLICATION.$KEY_APPLICATION_ID").`is`(id)
      }
    }
  }

  class Updates {
    companion object {
      fun updateCallback(details: Any): Update {
        val update = Update()
          .set(KEY_CALLBACK, details)
        update.inc(KEY_VERSION)
        return update
      }

      fun updateApplication(details: ApplicationDetails): Update {
        val update = Update()
          .set(KEY_APPLICATION, details)
        update.inc(KEY_VERSION)
        return update
      }

      fun unblockSubscriptionUpdate(): Update {
        return Update()
          .unset(KEY_BLOCK_DETAILS)
          .inc(KEY_VERSION, 1)
      }

      fun blockSubscriptionUpdate(details: BlockedDetailsDTO): Update {
        return Update()
          .set(KEY_BLOCK_DETAILS, details)
          .inc(KEY_VERSION, 1)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_TOPIC = "topic"
      const val KEY_ENTITY = "entity"
      const val KEY_BLOCK_DETAILS = "blockedDetails"
      const val KEY_APPLICATION = "application"
      const val KEY_CALLBACK = "callback"
      const val SUBSCRIPTION_COLLECTION_NAME = "subscription"
    }
  }
}
