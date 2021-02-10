package com.hookiesolutions.webhookie.webhook.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 10/2/21 19:13
 */
@Service
class WebhookGroupServiceDelegate(
  private val webhookGroupService: WebhookGroupService
) {
  fun providerTopics(): Flux<String> {
    return webhookGroupService.myTopics()
      .map { it.name }
  }
}