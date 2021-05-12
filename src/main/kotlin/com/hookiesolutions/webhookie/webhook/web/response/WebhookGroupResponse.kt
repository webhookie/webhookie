package com.hookiesolutions.webhookie.webhook.web.response

import com.fasterxml.jackson.annotation.JsonView
import com.hookiesolutions.webhookie.webhook.domain.ConsumerAccess
import com.hookiesolutions.webhookie.webhook.domain.ProviderAccess
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/1/21 13:36
 */
data class WebhookGroupResponse(
  @JsonView(WebhookGroupViews.Summary::class)
  val id: String,
  @JsonView(WebhookGroupViews.Summary::class)
  val title: String,
  @JsonView(WebhookGroupViews.Summary::class)
  val webhookVersion: String,
  @JsonView(WebhookGroupViews.Summary::class)
  val description: String?,
  @JsonView(WebhookGroupViews.Summary::class)
  val topics: List<Topic>,
  @JsonView(WebhookGroupViews.Full::class)
  val raw: String,
  @JsonView(WebhookGroupViews.Summary::class)
  val consumerAccess: ConsumerAccess,
  @JsonView(WebhookGroupViews.Full::class)
  val consumerGroups: Set<String>,
  @JsonView(WebhookGroupViews.Summary::class)
  val providerAccess: ProviderAccess,
  @JsonView(WebhookGroupViews.Full::class)
  val providerGroups: Set<String>
) {
  constructor(entity: WebhookGroup) : this(
    entity.id!!,
    entity.title,
    entity.webhookVersion,
    entity.description,
    entity.topics.map { Topic(it.name, it.description) },
    entity.raw,
    entity.consumerAccess,
    entity.consumerIAMGroups,
    entity.providerAccess,
    entity.providerIAMGroups
  )

  @JsonView(WebhookGroupViews.Summary::class)
  data class Topic(
    val name: String,
    val description: String?
  )
}
