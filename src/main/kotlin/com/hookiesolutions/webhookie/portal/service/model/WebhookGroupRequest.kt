package com.hookiesolutions.webhookie.portal.service.model

import com.hookiesolutions.webhookie.portal.domain.webhook.ConsumerAccess
import com.hookiesolutions.webhookie.portal.domain.webhook.ProviderAccess
import com.hookiesolutions.webhookie.portal.domain.webhook.WebhookGroup

data class WebhookGroupRequest(
  val spec: AsyncApiSpec,
  val consumerGroups: List<String>,
  val providerGroups: List<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
) {
  fun toWebhookGroup(): WebhookGroup {
    return WebhookGroup(
      this.spec.name,
      this.spec.version,
      this.spec.description,
      this.spec.topics,
      this.spec.raw,
      this.spec.spec,
      this.consumerGroups,
      this.providerGroups,
      this.consumerAccess,
      this.providerAccess
    )

  }
}