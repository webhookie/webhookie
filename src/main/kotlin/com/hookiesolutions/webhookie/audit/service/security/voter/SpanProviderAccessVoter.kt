package com.hookiesolutions.webhookie.audit.service.security.voter

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.webhook.service.WebhookApiServiceDelegate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/5/21 11:58
 */
@Component
class SpanProviderAccessVoter(
  private val webhookServiceDelegate: WebhookApiServiceDelegate,
) {
  fun vote(spanMono: Mono<Span>): Mono<Span> {
    val providerTopics = webhookServiceDelegate.providerTopicsConsideringAdmin()

    return spanMono
      .zipWith(providerTopics)
      .filter { it.t2.isAdmin || it.t2.topics.contains(it.t1.subscription.topic) }
      .map { it.t1 }
  }
}
