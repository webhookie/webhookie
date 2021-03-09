package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.SpanRepository
import com.hookiesolutions.webhookie.audit.domain.SpanRetry
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
    if(message.numberOfRetries == 1) {
      markAsRetrying(message)
    } else {
      addRetry(message)
    }
  }

  private fun markAsRetrying(message: SignableSubscriptionMessage) {
    log.info("Marking  '{}', '{}' as Retrying. ", message.spanId, message.traceId)
    val time = timeMachine.now()
    val retry = SpanRetry(time, message.numberOfRetries, message.delay.seconds)
    val statusUpdate = SpanStatusUpdate(SpanStatus.RETRYING, time)
    repository.addStatusUpdate(message.spanId, statusUpdate, retry)
      .subscribe { log.debug("'{}', '{}' Span was updated to: '{}'", it.spanId, it.traceId, it.lastStatus) }
  }

  private fun addRetry(message: SignableSubscriptionMessage) {
    log.info("Delaying '{}', '{}' span for '{}' seconds", message.spanId, message.traceId, message.delay.seconds)
    val time = timeMachine.now()
    val retry = SpanRetry(time, message.numberOfRetries, message.delay.seconds)
    repository.addRetry(message.spanId, retry)
      .subscribe { log.debug("'{}', '{}' Span was updated to: '{}'", it.spanId, it.traceId, it.lastStatus) }
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

/*
  fun updateWithServerError(message: PublisherResponseErrorMessage) {
    val time = timeMachine.now()
    val spanRetry = SpanRetry(
      time,
      message.subscriptionMessage.numberOfRetries,
      message.subscriptionMessage.delay.seconds,
      message.response.status.value()
    )

    val response = SpanServerResponse(time, message.response, message.subscriptionMessage.numberOfRetries)

    repository.updateWithResponse(message.spanId, spanRetry, response)
      .subscribe { log.debug("'{}', '{}' Span was updated with server response: '{}'", it.spanId, it.traceId, it.lastResponse?.response?.status) }
  }

  fun updateWithSuccessResponse(message: PublisherSuccessMessage) {
    val time = timeMachine.now()
    val statusUpdate = SpanStatusUpdate(SpanStatus.RETRYING, time)

    val response = SpanServerResponse(time, message.response, message.subscriptionMessage.numberOfRetries)

    repository.updateWithResponse(message.spanId, response, statusUpdate)
      .subscribe { log.debug("'{}', '{}' Span was updated with server response: '{}'", it.spanId, it.traceId, it.lastResponse?.response?.status) }
  }
*/

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
