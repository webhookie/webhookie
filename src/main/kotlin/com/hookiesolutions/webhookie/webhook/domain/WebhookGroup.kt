package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.fieldName
import com.hookiesolutions.webhookie.webhook.domain.Topic.Keys.Companion.KEY_TOPIC_NAME
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_CONSUMER_ACCESS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_CONSUMER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_PROVIDER_ACCESS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_PROVIDER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_WEBHOOK_GROUP_TOPIC
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.WEBHOOK_GROUP_COLLECTION_NAME
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where

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
  val topics: List<Topic>,
  val raw: String,
  val consumerIAMGroups: Set<String>,
  val providerIAMGroups: Set<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
  @Indexed(name = "webhook_group.numberOfTopics")
  val numberOfTopics: Int = topics.size
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
    }
  }

  class Keys {
    companion object {
      const val KEY_CONSUMER_IAM_GROUPS = "consumerIAMGroups"
      const val KEY_PROVIDER_IAM_GROUPS = "providerIAMGroups"
      const val KEY_CONSUMER_ACCESS = "consumerAccess"
      const val KEY_NUMBER_OF_TOPICS = "numberOfTopics"
      const val KEY_PROVIDER_ACCESS = "providerAccess"
      const val KEY_TOPICS = "topics"
      const val WEBHOOK_GROUP_COLLECTION_NAME = "webhook_group"
      val KEY_WEBHOOK_GROUP_TOPIC = fieldName(KEY_TOPICS, KEY_TOPIC_NAME)
    }
  }
}
