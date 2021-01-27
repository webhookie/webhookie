package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractDocument.Keys.Companion.KEY_VERSION
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.model.dto.Callback
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_BLOCK_DETAILS
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_COMPANY_ID
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_TOPIC
import org.springframework.data.annotation.TypeAlias
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
@Document(collection = "subscription")
@TypeAlias("subscription")
data class Subscription(
  val name: String,
  val companyId: String,
  val applicationId: String,
  @Indexed
  val topic: String,
  val callback: Callback,
  val blockedDetails: BlockedDetailsDTO? = null
) : AbstractEntity() {
  fun dto(): SubscriptionDTO {
    return SubscriptionDTO(
      id!!,
      name,
      companyId,
      applicationId,
      topic,
      callback,
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

      fun isAuthorized(companyIdSet: Set<String>): Criteria {
        return where(KEY_COMPANY_ID)
          .`in`(companyIdSet)
      }
    }
  }

  class Updates {
    companion object {
      fun unblockSubscription(): Update {
        return Update()
          .unset(KEY_BLOCK_DETAILS)
          .inc(KEY_VERSION, 1)
      }

      fun blockSubscription(details: BlockedDetailsDTO): Update {
        return Update()
          .set(KEY_BLOCK_DETAILS, details)
          .inc(KEY_VERSION, 1)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_TOPIC = "topic"
      const val KEY_COMPANY_ID = "companyId"
      const val KEY_BLOCK_DETAILS = "blockedDetails"
    }
  }
}
