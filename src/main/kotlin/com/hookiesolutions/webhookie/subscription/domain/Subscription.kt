package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
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
  val callbackSecurity: CallbackSecurity
) : AbstractEntity() {
  class Queries {
    companion object {
      fun topicIs(topic: String): Criteria {
        return where("topic").`is`(topic)
      }
    }
  }
}
