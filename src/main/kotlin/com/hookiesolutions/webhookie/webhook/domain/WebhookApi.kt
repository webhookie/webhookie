/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.fieldName
import com.hookiesolutions.webhookie.webhook.domain.Topic.Keys.Companion.KEY_TOPIC_NAME
import com.hookiesolutions.webhookie.webhook.domain.Webhook.Keys.Companion.KEY_NUMBER_OF_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.webhook.domain.Webhook.Keys.Companion.KEY_TOPIC
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_CONSUMER_ACCESS
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_CONSUMER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_PROVIDER_ACCESS
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_PROVIDER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_WEBHOOKS_No_OS_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_WEBHOOK_API_TOPIC
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.WEBHOOK_API_COLLECTION_NAME
import com.hookiesolutions.webhookie.webhook.service.model.AsyncApiSpec
import com.hookiesolutions.webhookie.webhook.service.model.WebhookApiRequest
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Update

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 16:08
 */
@Document(collection = WEBHOOK_API_COLLECTION_NAME)
@TypeAlias("webhookApi")
data class WebhookApi(
  val title: String,
  val webhookVersion: String,
  val description: String?,
  val webhooks: List<Webhook>,
  val raw: String,
  val consumerIAMGroups: Set<String>,
  val providerIAMGroups: Set<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
  val approvalDetails: WebhookApiApprovalDetails,
  @Indexed(name = "webhook_api.numberOfWebhooks")
  val numberOfWebhooks: Int = webhooks.size
) : AbstractEntity() {
  class Queries {
    companion object {
      private fun consumerGroupsIn(groups: Collection<String>): Criteria {
        return where(KEY_CONSUMER_IAM_GROUPS).`in`(groups)
      }

      private fun providerGroupsIn(groups: Collection<String>): Criteria {
        return where(KEY_PROVIDER_IAM_GROUPS).`in`(groups)
      }

      private fun publicForConsumers(): Criteria {
        return where(KEY_CONSUMER_ACCESS).`is`(ConsumerAccess.PUBLIC)
      }

      private fun accessibleForAllProviders(): Criteria {
        return where(KEY_PROVIDER_ACCESS).`is`(ProviderAccess.ALL)
      }

      fun webhookApiTopicIs(topic: String): Criteria {
        return where(KEY_WEBHOOK_API_TOPIC).`is`(topic)
      }

      fun accessibleForProvider(groups: Collection<String>): Criteria {
        return Criteria()
          .orOperator(
            accessibleForAllProviders(),
            providerGroupsIn(groups)
          )
      }

      fun accessibleForGroups(groups: Collection<String>): Criteria {
        return Criteria()
          .orOperator(
            publicForConsumers(),
            consumerGroupsIn(groups),
            providerGroupsIn(groups),
          )
      }

      fun elemMatchByTopic(topic: String): Criteria {
        return where(Keys.KEY_WEBHOOKS).elemMatch(
            where(fieldName(KEY_TOPIC, KEY_TOPIC_NAME)).`is`(topic)
          )
      }
    }
  }

  class Updates {
    companion object {
      fun incNumberOfSubscriptions(number: Int): Update {
        return Update()
          .inc(KEY_WEBHOOKS_No_OS_SUBSCRIPTIONS, number)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_CONSUMER_IAM_GROUPS = "consumerIAMGroups"
      const val KEY_PROVIDER_IAM_GROUPS = "providerIAMGroups"
      const val KEY_CONSUMER_ACCESS = "consumerAccess"
      const val KEY_NUMBER_OF_WEBHOOKS = "numberOfWebhooks"
      const val KEY_PROVIDER_ACCESS = "providerAccess"
      const val KEY_WEBHOOKS = "webhooks"
      const val KEY_APPROVAL_DETAILS = "approvalDetails"
      const val WEBHOOK_API_COLLECTION_NAME = "webhook_api"
      val KEY_WEBHOOK_API_TOPIC = fieldName(KEY_WEBHOOKS, KEY_TOPIC, KEY_TOPIC_NAME)
      val KEY_WEBHOOKS_No_OS_SUBSCRIPTIONS = fieldName(KEY_WEBHOOKS, "$", KEY_NUMBER_OF_SUBSCRIPTIONS)
    }
  }

  companion object {
    fun create(
      spec: AsyncApiSpec,
      request: WebhookApiRequest,
      webhooks: List<Webhook>? = null
    ): WebhookApi {
      return WebhookApi(
        spec.name,
        spec.version,
        spec.description,
        webhooks ?: spec.topics.map { Webhook(it) },
        request.asyncApiSpec,
        request.consumerGroups,
        request.providerGroups,
        request.consumerAccess,
        request.providerAccess,
        WebhookApiApprovalDetails(request.requiresApproval)
      )
    }

    fun create(
      spec: AsyncApiSpec,
      request: WebhookApiRequest,
      webhookApi: WebhookApi
    ): WebhookApi {
      val webhooks = spec.topics
        .map { topic ->
          val numberOfSubscriptions = webhookApi.webhooks
            .firstOrNull { it.topic.name == topic.name }
            ?.numberOfSubscriptions ?: 0

          Webhook(topic, numberOfSubscriptions)
        }

      return create(spec, request, webhooks)
    }
  }
}
