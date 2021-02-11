package com.hookiesolutions.webhookie.webhook.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 10/2/21 19:13
 */
@Service
class WebhookGroupServiceDelegate(
  private val webhookGroupService: WebhookGroupService
) {
  fun providerTopics(): Mono<List<String>> {
    return webhookGroupService.myTopics()
      .map { it.name }
      .collectList()
  }
}