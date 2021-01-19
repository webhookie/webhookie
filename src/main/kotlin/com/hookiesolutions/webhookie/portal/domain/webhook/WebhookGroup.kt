package com.hookiesolutions.webhookie.portal.domain.webhook

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.portal.domain.webhook.WebhookGroup.Keys.Companion.KEY_CONSUMER_ACCESS
import com.hookiesolutions.webhookie.portal.domain.webhook.WebhookGroup.Keys.Companion.KEY_CONSUMER_IAM_GROUPS
import com.hookiesolutions.webhookie.portal.domain.webhook.WebhookGroup.Keys.Companion.KEY_PROVIDER_IAM_GROUPS
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 16:08
 */
@Document(collection = "webhook_group")
@TypeAlias("webhookGroup")
data class WebhookGroup(
  val name: String,
  val webhookVersion: String,
  val description: String?,
  val topics: List<Topic>,
  val raw: String,
  val spec: Map<String, Any>,
  val consumerIAMGroups: List<String>,
  val providerIAMGroups: List<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
): AbstractEntity() {
  class Queries {
    companion object {
      fun consumerGroupsIn(groups: List<String>): Criteria {
        return where(KEY_CONSUMER_IAM_GROUPS).`in`(groups)
      }

      private fun providerGroupsIn(groups: List<String>): Criteria {
        return where(KEY_PROVIDER_IAM_GROUPS).`in`(groups)
      }

      private fun publicForConsumers(): Criteria {
        return where(KEY_CONSUMER_ACCESS).`is`(ConsumerAccess.PUBLIC)
      }

      fun accessibleForProviderWith(groups: List<String>): Criteria {
        return Criteria()
          .orOperator(
            consumerGroupsIn(groups),
            providerGroupsIn(groups),
            publicForConsumers()
          )
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_CONSUMER_IAM_GROUPS = "consumerIAMGroups"
      const val KEY_PROVIDER_IAM_GROUPS = "providerIAMGroups"
      const val KEY_CONSUMER_ACCESS = "consumerAccess"
    }
  }
}
