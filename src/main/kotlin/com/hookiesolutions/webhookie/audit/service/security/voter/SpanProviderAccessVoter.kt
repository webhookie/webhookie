package com.hookiesolutions.webhookie.audit.service.security.voter

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.webhook.service.WebhookGroupServiceDelegate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/5/21 11:58
 */
@Component
class SpanProviderAccessVoter(
  private val webhookServiceDelegate: WebhookGroupServiceDelegate,
) {
  fun vote(spanMono: Mono<Span>): Mono<Span> {
    val providerTopics = webhookServiceDelegate.providerTopics()
      .onErrorReturn(emptyList())

    return spanMono
      .zipWith(providerTopics)
      .filter { it.t2.contains(it.t1.subscription.topic) }
      .map { it.t1 }
  }
}
