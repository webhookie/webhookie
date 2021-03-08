package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.SpanRepository
import com.hookiesolutions.webhookie.audit.domain.SpanStatus
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.service.TimeMachine
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/3/21 12:41
 */
@Service
class SpanService(
  private val repository: SpanRepository,
  private val timeMachine: TimeMachine,
  private val log: Logger
) {
  fun createSpan(message: SignableSubscriptionMessage) {
    log.info("'{}', '{}' span to be saved", message.spanId, message.traceId)
    val span = Span.Builder()
      .message(message)
      .status(SpanStatus.PROCESSING)
      .time(timeMachine.now())
      .build()

    saveOrFetch(span)
      .subscribe { log.debug("'{}', '{}' span was saved/fetched", it.spanId, it.traceId) }
  }

  fun addRetry(message: SignableSubscriptionMessage) {
    log.info("Delaying '{}', '{}' span for '{}' seconds", message.spanId, message.traceId, message.delay.seconds)
    val statusUpdate = SpanStatusUpdate(SpanStatus.RETRYING, timeMachine.now())
    repository.addStatusUpdate(message.traceId, message.spanId, statusUpdate)
      .subscribe { log.debug("'{}', '{}' Span was updated to: '{}'", it.spanId, it.traceId, it.statusUpdate) }
  }

  private fun saveOrFetch(span: Span): Mono<Span> {
    val spanId = span.spanId
    return repository.save(span)
      .doOnNext { log.info("'{}', '{}' Span saved successfully: '{}'", spanId, it.traceId, it.id) }
      .onErrorResume(EntityExistsException::class.java) {
        log.warn("'{}' Span already exists! fetching the existing document...", spanId)
        repository.findBySpanId(spanId)
      }
  }
}
