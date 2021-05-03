package com.hookiesolutions.webhookie.audit.service.security.voter

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.service.SubscriptionServiceDelegate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/5/21 11:57
 */
@Component
class SpanConsumerAccessVoter(
  private val subscriptionServiceDelegate: SubscriptionServiceDelegate,
) {
  fun vote(spanMono: Mono<Span>): Mono<Span> {
    val userApplicationsMono = subscriptionServiceDelegate.userApplications()
      .collectList()
      .onErrorReturn(emptyList())

    return spanMono
      .zipWith(userApplicationsMono)
      .filter { it.t2.contains(it.t1.subscription.application) }
      .map { it.t1 }
  }
}
