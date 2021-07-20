package com.hookiesolutions.webhookie.webhook.web.response

import com.fasterxml.jackson.annotation.JsonView
import com.hookiesolutions.webhookie.webhook.domain.ConsumerAccess
import com.hookiesolutions.webhookie.webhook.domain.ProviderAccess
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/1/21 13:36
 */
data class WebhookApiResponse(
  @JsonView(WebhookApiViews.Summary::class)
  val id: String,
  @JsonView(WebhookApiViews.Summary::class)
  val title: String,
  @JsonView(WebhookApiViews.Summary::class)
  val webhookVersion: String,
  @JsonView(WebhookApiViews.Summary::class)
  val description: String?,
  @JsonView(WebhookApiViews.Summary::class)
  val webhooks: List<Webhook>,
  @JsonView(WebhookApiViews.Full::class)
  val raw: String,
  @JsonView(WebhookApiViews.Summary::class)
  val consumerAccess: ConsumerAccess,
  @JsonView(WebhookApiViews.Full::class)
  val consumerGroups: Set<String>,
  @JsonView(WebhookApiViews.Summary::class)
  val providerAccess: ProviderAccess,
  @JsonView(WebhookApiViews.Full::class)
  val providerGroups: Set<String>
) {
  constructor(entity: WebhookApi) : this(
    entity.id!!,
    entity.title,
    entity.webhookVersion,
    entity.description,
    entity.webhooks.map { Webhook(Topic(it.topic.name, it.topic.description), it.numberOfSubscriptions) },
    entity.raw,
    entity.consumerAccess,
    entity.consumerIAMGroups,
    entity.providerAccess,
    entity.providerIAMGroups
  )

  @JsonView(WebhookApiViews.Summary::class)
  data class Webhook(
    val topic: Topic,
    val numberOfSubscriptions: Int
  )

  @JsonView(WebhookApiViews.Summary::class)
  data class Topic(
    val name: String,
    val description: String?
  )
}
