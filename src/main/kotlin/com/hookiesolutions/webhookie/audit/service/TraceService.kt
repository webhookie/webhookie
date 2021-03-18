package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.Trace
import com.hookiesolutions.webhookie.audit.domain.TraceRepository
import com.hookiesolutions.webhookie.audit.domain.TraceStatus
import com.hookiesolutions.webhookie.audit.domain.TraceStatusUpdate
import com.hookiesolutions.webhookie.audit.domain.TraceSummary
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.WebhookieMessage
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
import com.hookiesolutions.webhookie.common.service.TimeMachine
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/3/21 19:06
 */
@Service
class TraceService(
  private val repository: TraceRepository,
  private val timeMachine: TimeMachine,
  private val log: Logger,
) {
  fun save(message: ConsumerMessage) {
    log.info("Saving new Consumer Message as trace with traceId: '{}'", message.traceId)
    val trace = Trace.Builder()
      .message(message)
      .time(timeMachine.now())
      .build()
    saveOrFetch(trace)
      .subscribe { log.debug("'{}' trace was saved/fetched", it.traceId) }
  }

  fun updateWithNoSubscription(message: NoSubscriptionMessage) {
    addStatus(message.traceId, TraceStatus.NO_SUBSCRIPTION)
  }

  fun updateWithIssues(message: WebhookieMessage) {
    addStatus(message.traceId, TraceStatus.ISSUES)
  }

  private fun addStatus(traceId: String, status: TraceStatus) {
    log.info("Updating trace({}) with '{}'", traceId, status.name)

    repository.addStatus(traceId, TraceStatusUpdate(status, timeMachine.now()))
      .subscribe { log.debug("'{}' trace was updated to '{}'", it.traceId, it.statusUpdate) }
  }

  private fun saveOrFetch(trace: Trace): Mono<Trace> {
    val traceId = trace.traceId
    return repository.save(trace)
      .doOnNext { log.info("'{}' trace saved successfully: '{}'", traceId, it.id) }
      .onErrorResume(EntityExistsException::class.java) {
        log.info("'{}' Trace already exists! fetching the existing document...", traceId)
        repository.findByTraceId(traceId)
      }
  }

  fun updateWithSummary(traceId: String, summary: TraceSummary) {
    val status = if(summary.isOK()) {
      TraceStatus.OK
    } else {
      TraceStatus.ISSUES
    }
    log.info("Updating trace({}) with status: '{}', summary: '{}'", traceId, status, summary)

    repository.updateWithSummary(traceId, summary, TraceStatusUpdate(status, timeMachine.now()))
      .subscribe { log.debug("'{}' trace was updated with '{}'", it.traceId, it.summary) }
  }

  fun increaseSuccessResponse(traceId: String) {
    log.info("Increasing number of success spans for  trace({})", traceId)

    repository.increaseSuccessSpan(traceId, timeMachine.now())
      .subscribe { log.debug("'{}' trace was updated with '{}', '{}'", it.traceId, it.summary, it.statusUpdate.status) }
  }
}
