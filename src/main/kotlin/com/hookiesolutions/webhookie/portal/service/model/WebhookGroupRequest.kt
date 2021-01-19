package com.hookiesolutions.webhookie.portal.service.model

import com.hookiesolutions.webhookie.portal.domain.webhook.ConsumerAccess
import com.hookiesolutions.webhookie.portal.domain.webhook.ProviderAccess
import com.hookiesolutions.webhookie.portal.domain.webhook.WebhookGroup

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