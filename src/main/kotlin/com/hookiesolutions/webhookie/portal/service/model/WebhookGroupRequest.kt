package com.hookiesolutions.webhookie.portal.service.model

import com.hookiesolutions.webhookie.portal.domain.ConsumerAccess
import com.hookiesolutions.webhookie.portal.domain.ProviderAccess

data class WebhookGroupRequest(
  val spec: AsyncApiSpec,
  val consumerGroups: List<String>,
  val providerGroups: List<String>,
  val consumerAccess: ConsumerAccess,
  val providerAccess: ProviderAccess,
)