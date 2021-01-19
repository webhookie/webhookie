package com.hookiesolutions.webhookie.webhook.service.model

import com.hookiesolutions.webhookie.webhook.domain.ConsumerAccess
import com.hookiesolutions.webhookie.webhook.domain.ProviderAccess
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup

data class WebhookGroupRequest(
  val asyncApiSpec: AsyncApiSpec,
  val consumerGroups: List<String>,
  val providerGroups: List<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
) {
  fun toWebhookGroup(): WebhookGroup {
    return WebhookGroup(
      this.asyncApiSpec.name,
      this.asyncApiSpec.version,
      this.asyncApiSpec.description,
      this.asyncApiSpec.topics,
      this.asyncApiSpec.raw,
      this.asyncApiSpec.spec,
      this.consumerGroups,
      this.providerGroups,
      this.consumerAccess,
      this.providerAccess
    )

  }
}