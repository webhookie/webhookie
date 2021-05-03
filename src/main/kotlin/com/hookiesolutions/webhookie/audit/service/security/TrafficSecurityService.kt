package com.hookiesolutions.webhookie.audit.service.security

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.Trace
import com.hookiesolutions.webhookie.audit.service.security.voter.SpanConsumerAccessVoter
import com.hookiesolutions.webhookie.audit.service.security.voter.SpanProviderAccessVoter
import com.hookiesolutions.webhookie.audit.service.security.voter.TraceConsumerAccessVoter
import com.hookiesolutions.webhookie.audit.service.security.voter.TraceProviderAccessVoter
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/5/21 11:26
 */
@Component
class TrafficSecurityService(
  private val spanConsumerAccessVoter: SpanConsumerAccessVoter,
  private val spanProviderAccessVoter: SpanProviderAccessVoter,
  private val traceConsumerAccessVoter: TraceConsumerAccessVoter,
  private val traceProviderAccessVoter: TraceProviderAccessVoter
) {
  fun verifyTraceReadAccess(traceMono: Mono<Trace>): Mono<Trace> {
    return traceProviderAccessVoter.vote(traceMono)
      .switchIfEmpty { traceConsumerAccessVoter.vote(traceMono) }
      .switchIfEmpty { AccessDeniedException("Access Denied!").toMono() }
  }

  fun verifySpanReadAccess(spanMono: Mono<Span>): Mono<Span> {
    return spanConsumerAccessVoter.vote(spanMono)
      .switchIfEmpty { spanProviderAccessVoter.vote(spanMono) }
      .switchIfEmpty { AccessDeniedException("Access Denied!").toMono() }
  }
}
