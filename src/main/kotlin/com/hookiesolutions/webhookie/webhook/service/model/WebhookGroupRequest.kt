package com.hookiesolutions.webhookie.webhook.service.model

import com.hookiesolutions.webhookie.webhook.domain.ConsumerAccess
import com.hookiesolutions.webhookie.webhook.domain.ProviderAccess

data class WebhookGroupRequest(
  val asyncApiSpec: String,
  val consumerGroups: Set<String>,
  val providerGroups: Set<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
)
