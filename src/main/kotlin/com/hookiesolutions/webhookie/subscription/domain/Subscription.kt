package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractDocument.Keys.Companion.KEY_VERSION
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails.Keys.Companion.KEY_APPLICATION_ID
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails.Keys.Companion.KEY_CALLBACK_ID
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Keys.Companion.KEY_STATUS
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.subscription.domain.Application.Keys.Companion.KEY_ENTITY
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_APPLICATION
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_CALLBACK
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_STATUS_UPDATE
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
    name = "subscription_callback_topic",
    def = "{'$KEY_CALLBACK.$KEY_CALLBACK_ID' : 1, $KEY_TOPIC: 1}",
    unique = true
  ),
  CompoundIndex(
    name = "subscription_status_topic",
    def = "{'$KEY_STATUS_UPDATE.$KEY_STATUS' : 1, $KEY_TOPIC: 1}"
  )
)
data class Subscription(
  @Indexed(name = "subscription_topic")
  val topic: String,
  val application: ApplicationDetails,
  val callback: CallbackDetails,
  val statusUpdate: StatusUpdate
) : AbstractEntity() {
  fun dto(): SubscriptionDTO {
    return SubscriptionDTO(
      id!!,
      application,
      topic,
      callback.dto(),
      statusUpdate
    )
  }

  class Queries {
    companion object {
      fun topicIs(topic: String): Criteria {
        return where(KEY_TOPIC).`is`(topic)
      }

      fun topicIsIn(topics: Collection<String>): Criteria {
        return where(KEY_TOPIC).`in`(topics)
      }

      fun statusIsIn(statusUpdateList: List<SubscriptionStatus>): Criteria {
        return where("$KEY_STATUS_UPDATE.$KEY_STATUS").`in`(statusUpdateList.map { it.name })
      }

      fun isAuthorized(entities: Set<String>): Criteria {
        return where("$KEY_APPLICATION.$KEY_ENTITY")
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

      fun subscriptionStatusUpdate(statusUpdate: StatusUpdate): Update {
        val update = Update()
          .set(KEY_STATUS_UPDATE, statusUpdate)
        update.inc(KEY_VERSION)
        return update
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_TOPIC = "topic"
      const val KEY_APPLICATION = "application"
      const val KEY_CALLBACK = "callback"
      const val KEY_STATUS_UPDATE = "statusUpdate"
      const val SUBSCRIPTION_COLLECTION_NAME = "subscription"
    }
  }
}
