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
  fun toWebhookGroup(id: String? = null): WebhookGroup {
    val webhookGroup = WebhookGroup(
      this.asyncApiSpec.name,
      this.asyncApiSpec.version,
      this.asyncApiSpec.description,
      this.asyncApiSpec.topics,
      this.asyncApiSpec.raw,
      this.consumerGroups,
      this.providerGroups,
      this.consumerAccess,
      this.providerAccess
    )
    webhookGroup.id = id
    return webhookGroup
  }
}