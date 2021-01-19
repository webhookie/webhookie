package com.hookiesolutions.webhookie.portal.service.model

import com.hookiesolutions.webhookie.portal.domain.webhook.ConsumerAccess
import com.hookiesolutions.webhookie.portal.domain.webhook.ProviderAccess

data class WebhookGroupRequest(
  val spec: AsyncApiSpec,
  val consumerGroups: List<String>,
  val providerGroups: List<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
)