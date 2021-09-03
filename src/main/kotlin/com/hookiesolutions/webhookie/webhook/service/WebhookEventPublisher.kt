package com.hookiesolutions.webhookie.webhook.service

import com.hookiesolutions.webhookie.webhook.domain.WebhookApi
import org.slf4j.Logger
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class WebhookEventPublisher(
  private val webhookApiDeletedChannel: MessageChannel,
  private val log: Logger
) {
  fun publishWebhookApiDeletedEvent(api: WebhookApi) {
    val topics = api.webhooks.map { it.topic.name }
    log.info("Publishing deleted '{}' topics event to suspend subscriptions", api.title, topics.size)
    webhookApiDeletedChannel.send(MessageBuilder.withPayload(topics).build())
  }
}
