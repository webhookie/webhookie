package com.hookiesolutions.webhookie.audit.service.security.voter

import com.hookiesolutions.webhookie.audit.domain.Trace
import com.hookiesolutions.webhookie.audit.service.SubscriptionServiceDelegate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/5/21 12:09
 */
@Component
class TraceConsumerAccessVoter(
  private val subscriptionServiceDelegate: SubscriptionServiceDelegate,
) {
  fun vote(traceMono: Mono<Trace>): Mono<Trace> {
    val topicsMono = subscriptionServiceDelegate.consumerTopics()
      .onErrorReturn(emptyList())

    return traceMono
      .zipWith(topicsMono)
      .filter { it.t2.contains(it.t1.topic) }
      .map { it.t1 }
  }
}
