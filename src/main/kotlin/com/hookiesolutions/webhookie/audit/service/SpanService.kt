package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.*
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate.Companion.notOk
import com.hookiesolutions.webhookie.audit.web.model.request.SpanRequest
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.webhook.service.WebhookGroupServiceDelegate
import org.slf4j.Logger
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/3/21 12:41
 */
@Service
class SpanService(
  private val repository: SpanRepository,
  private val timeMachine: TimeMachine,
  private val webhookServiceDelegate: WebhookGroupServiceDelegate,
  private val subscriptionServiceDelegate: SubscriptionServiceDelegate,
  private val securityHandler: SecurityHandler,
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

  fun blockSpan(message: BlockedSubscriptionMessageDTO) {
    log.info("Blocking '{}', '{}' span. reason:", message.spanId, message.traceId, message.blockedDetails.reason)
    val statusUpdate = SpanStatusUpdate.blocked(timeMachine.now())
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

  fun updateWithServerError(message: PublisherResponseErrorMessage) {
      log.info("Updating span '{}', '{}' with server error", message.spanId, message.traceId, message.response.status)
      val time = timeMachine.now()

      val response = SpanResult.Builder()
        .time(time)
        .message(message)
        .build()

      repository.updateWithResponse(message.spanId, response)
        .subscribe { log.info("'{}', '{}' Span was updated with server response: '{}'", it.spanId, it.traceId, it.latestResult) }
  }

  fun updateWithClientError(message: PublisherRequestErrorMessage) {
    log.info("Updating span '{}', '{}' with request error", message.spanId, message.traceId, message.reason)
    val time = timeMachine.now()

    val response = SpanResult.Builder()
      .time(time)
      .message(message)
      .build()

    repository.updateWithResponse(message.spanId, response)
      .subscribe { log.info("'{}', '{}' Span was updated with client error response: '{}'", it.spanId, it.traceId, it.latestResult?.statusCode) }
  }

  fun updateWithOtherError(message: PublisherOtherErrorMessage) {
    log.info("Updating span '{}', '{}' with unknown error", message.spanId, message.traceId, message.reason)
    val time = timeMachine.now()

    val response = SpanResult.Builder()
      .time(time)
      .message(message)
      .build()

    repository.addStatusUpdate(message.spanId, notOk(time), response = response)
      .subscribe { log.info("'{}', '{}' Span was updated with other error response: '{}'", it.spanId, it.traceId, it.latestResult?.statusCode) }
  }

  fun updateWithSuccessResponse(message: PublisherSuccessMessage) {
    log.info("Updating span '{}', '{}' with SUCCESS", message.spanId, message.traceId, message.response.status)
    val time = timeMachine.now()

    val statusUpdate = SpanStatusUpdate.ok(timeMachine.now())
    val response = SpanResult.Builder()
      .time(time)
      .message(message)
      .build()

    repository.addStatusUpdate(message.spanId, statusUpdate, response = response)
      .subscribe { log.debug("'{}', '{}' Span was updated with server response: '{}'", it.spanId, it.traceId, it.latestResult?.statusCode) }
  }

  private fun markAsRetrying(message: SignableSubscriptionMessage) {
    log.info("Marking  '{}', '{}' as Retrying. ", message.spanId, message.traceId)
    val time = timeMachine.now().plusSeconds(message.delay.seconds)
    val retry = SpanRetry(time, message.numberOfRetries)
    val statusUpdate = SpanStatusUpdate.retrying(time)
    repository.addStatusUpdate(message.spanId, statusUpdate, retry = retry)
      .subscribe { log.debug("'{}', '{}' Span was updated to: '{}'", it.spanId, it.traceId, it.lastStatus) }
  }

  private fun addRetry(message: SignableSubscriptionMessage) {
    log.info("Delaying '{}', '{}' span for '{}' seconds", message.spanId, message.traceId, message.delay.seconds)
    val time = timeMachine.now().plusSeconds(message.delay.seconds)
    val retry = SpanRetry(time, message.numberOfRetries)
    repository.addRetry(message.spanId, retry)
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

  @PreAuthorize("hasAnyAuthority('$ROLE_CONSUMER', '$ROLE_ADMIN')")
  fun userSpans(pageable: Pageable, request: SpanRequest): Flux<Span> {
    return securityHandler.data()
      .map { it.hasAdminAuthority() }
      .zipWhen { hasAdminAuthority ->
        return@zipWhen if(hasAdminAuthority) {
          log.info("Fetching all spans form ADMIN")
          emptyList<String>().toMono()
        } else {
          subscriptionServiceDelegate.userApplications()
            .map { it.applicationId }
            .collectList()
        }
      }
      .flatMapMany {
        log.info("Fetching all spans by applications: '{}'", it)
        repository.userSpans(it.t2, request, it.t1, pageable)
      }
  }

  @PreAuthorize("hasAnyAuthority('$ROLE_CONSUMER', '$ROLE_ADMIN')")
  fun traceSpans(pageable: Pageable, traceId: String, request: TraceRequest): Flux<Span> {
    return webhookServiceDelegate.providerTopics()
      .flatMapMany { repository.traceSpans(pageable, traceId, it, request) }
  }

  fun fetchSpanVerifyingReadAccess(spanId: String): Mono<Span> {
    return repository.findBySpanIdVerifyingReadAccess(spanId)
  }
}
