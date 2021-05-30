package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.fieldName
import com.hookiesolutions.webhookie.webhook.domain.Topic.Keys.Companion.KEY_TOPIC_NAME
import com.hookiesolutions.webhookie.webhook.domain.Webhook.Keys.Companion.KEY_NUMBER_OF_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.webhook.domain.Webhook.Keys.Companion.KEY_TOPIC
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_CONSUMER_ACCESS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_CONSUMER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_PROVIDER_ACCESS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_PROVIDER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_WEBHOOKS_No_OS_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_WEBHOOK_GROUP_TOPIC
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.WEBHOOK_GROUP_COLLECTION_NAME
import com.hookiesolutions.webhookie.webhook.service.model.AsyncApiSpec
import com.hookiesolutions.webhookie.webhook.service.model.WebhookGroupRequest
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
@Document(collection = WEBHOOK_GROUP_COLLECTION_NAME)
@TypeAlias("webhookGroup")
data class WebhookGroup(
  val title: String,
  val webhookVersion: String,
  val description: String?,
  val webhooks: List<Webhook>,
  val raw: String,
  val consumerIAMGroups: Set<String>,
  val providerIAMGroups: Set<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
  @Indexed(name = "webhook_group.numberOfWebhooks")
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

      fun webhookGroupTopicIs(topic: String): Criteria {
        return where(KEY_WEBHOOK_GROUP_TOPIC).`is`(topic)
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
      fun increaseNumberOfSubscriptions(): Update {
        return Update()
          .inc(KEY_WEBHOOKS_No_OS_SUBSCRIPTIONS, 1)
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
      const val WEBHOOK_GROUP_COLLECTION_NAME = "webhook_group"
      val KEY_WEBHOOK_GROUP_TOPIC = fieldName(KEY_WEBHOOKS, KEY_TOPIC, KEY_TOPIC_NAME)
      val KEY_WEBHOOKS_No_OS_SUBSCRIPTIONS = fieldName(KEY_WEBHOOKS, "$", KEY_NUMBER_OF_SUBSCRIPTIONS)
    }
  }

  companion object {
    fun create(
      spec: AsyncApiSpec,
      request: WebhookGroupRequest,
      webhooks: List<Webhook>? = null
    ): WebhookGroup {
      return WebhookGroup(
        spec.name,
        spec.version,
        spec.description,
        webhooks ?: spec.topics.map { Webhook(it) },
        request.asyncApiSpec,
        request.consumerGroups,
        request.providerGroups,
        request.consumerAccess,
        request.providerAccess
      )
    }

    fun create(
      spec: AsyncApiSpec,
      request: WebhookGroupRequest,
      webhookApi: WebhookGroup
    ): WebhookGroup {
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
