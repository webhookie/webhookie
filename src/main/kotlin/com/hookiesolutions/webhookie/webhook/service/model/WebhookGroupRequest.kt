package com.hookiesolutions.webhookie.webhook.service.model

import com.hookiesolutions.webhookie.webhook.domain.ConsumerAccess
import com.hookiesolutions.webhookie.webhook.domain.ProviderAccess
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup

data class WebhookGroupRequest(
  val asyncApiSpec: AsyncApiSpec,
  val consumerGroups: Set<String>,
  val providerGroups: Set<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
) {
  fun toWebhookGroup(): WebhookGroup {
    return WebhookGroup(
      asyncApiSpec.name,
      asyncApiSpec.version,
      asyncApiSpec.description,
      asyncApiSpec.topics,
      asyncApiSpec.raw,
      consumerGroups,
      providerGroups,
      consumerAccess,
      providerAccess
    )
  }
}