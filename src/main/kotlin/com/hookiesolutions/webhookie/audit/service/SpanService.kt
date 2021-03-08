package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.SpanRepository
import com.hookiesolutions.webhookie.audit.domain.SpanStatus
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.service.TimeMachine
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

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

  fun retrying(message: SignableSubscriptionMessage) {
    log.info("Delaying '{}', '{}' span for '{}' seconds", message.spanId, message.traceId, message.delay.seconds)
    val time = timeMachine.now()
    val statusUpdate = SpanStatusUpdate(SpanStatus.RETRYING, time)
    repository.addStatusUpdate(message.spanId, statusUpdate)
      .subscribe { log.debug("'{}', '{}' Span was updated to: '{}'", it.spanId, it.traceId, it.lastRetry) }
  }

  fun blockSpan(message: BlockedSubscriptionMessageDTO) {
    log.info("Blocking '{}', '{}' span. reason:", message.spanId, message.traceId, message.blockedDetails.reason)
    val statusUpdate = SpanStatusUpdate(SpanStatus.BLOCKED, timeMachine.now())
    repository.addStatusUpdate(message.spanId, statusUpdate)
      .switchIfEmpty {
        val span = Span.Builder()
          .message(message)
          .status(SpanStatus.BLOCKED)
          .time(timeMachine.now())
          .build()

        saveOrFetch(span)
      }
      .subscribe { log.debug("'{}', '{}' Span was updated to: '{}'", it.spanId, it.traceId, it.lastStatus) }
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
