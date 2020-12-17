package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractDocument.Keys.Companion.KEY_VERSION
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.model.dto.CallbackSecurityDTO
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_BLOCK_DETAILS
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.HttpMethod

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:27
 */
@Document(collection = "subscription")
data class Subscription(
  val name: String,
  val companyId: String,
  val applicationId: String,
  @Indexed
  val topic: String,
  val callbackUrl: String,
  val httpMethod: HttpMethod,
  val callbackSecurity: CallbackSecurityDTO,
  val blockedDetails: BlockedDetailsDTO? = null
) : AbstractEntity() {
  fun subscriptionMessage(consumerMessage: ConsumerMessage, spanId: String): GenericSubscriptionMessage {
    return SubscriptionMessage(
      consumerMessage,
      spanId,
      dto()
    )
  }

  private fun dto(): SubscriptionDTO {
    return SubscriptionDTO(
      id!!,
      name,
      topic,
      callbackUrl,
      httpMethod,
      callbackSecurity,
      blockedDetails
    )
  }

  class Queries {
    companion object {
      fun topicIs(topic: String): Criteria {
        return where("topic").`is`(topic)
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
      const val KEY_BLOCK_DETAILS = "blockedDetails"
    }
  }
}
