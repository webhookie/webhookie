package com.hookiesolutions.webhookie.portal.domain.webhook

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

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
): AbstractEntity()
