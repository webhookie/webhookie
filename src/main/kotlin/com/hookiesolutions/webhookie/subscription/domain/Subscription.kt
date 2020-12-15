package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.model.dto.CallbackSecurityDTO
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.http.HttpMethod

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:27
 */
data class Subscription(
  val name: String,
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

  class Keys {
    companion object {
      const val KEY_TOPIC = "topic"
      const val KEY_BLOCK_DETAILS = "blockedDetails"
    }
  }
}
