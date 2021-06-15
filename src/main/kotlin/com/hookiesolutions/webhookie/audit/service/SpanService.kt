package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.SpanRepository
import com.hookiesolutions.webhookie.audit.domain.SpanResult
import com.hookiesolutions.webhookie.audit.domain.SpanRetry
import com.hookiesolutions.webhookie.audit.domain.SpanRetry.Companion.SENT_BY_WEBHOOKIE
import com.hookiesolutions.webhookie.audit.domain.SpanSendReason
import com.hookiesolutions.webhookie.audit.domain.SpanStatus
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate.Companion.blocked
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate.Companion.notOk
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate.Companion.ok
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate.Companion.retryingSpan
import com.hookiesolutions.webhookie.audit.domain.TraceRepository
import com.hookiesolutions.webhookie.audit.web.model.request.SpanRequest
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.HEADER_CONTENT_TYPE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_REQUESTED_BY
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_RESENT
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_SPAN_ID
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TOPIC
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.ResendSpanMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.webhook.service.WebhookGroupServiceDelegate
import org.slf4j.Logger
import org.springframework.data.domain.Pageable
import org.springframework.integration.core.GenericSelector
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/3/21 12:41
 */
@Service
class SpanService(
  private val repository: SpanRepository,
  private val traceRepository: TraceRepository,
  private val timeMachine: TimeMachine,
  private val webhookServiceDelegate: WebhookGroupServiceDelegate,
  private val factory: TrafficConversionFactory,
  private val subscriptionServiceDelegate: SubscriptionServiceDelegate,
  private val resendSpanChannel: MessageChannel,
  private val securityHandler: SecurityHandler,
  private val retryableErrorSelector: GenericSelector<GenericPublisherMessage>,
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

  fun retrying(message: Message<SignableSubscriptionMessage>) {
    if(message.payload.isFirstRetryInCycle()) {
      markAsRetrying(message)
    } else {
      addRetry(message.payload)
    }
  }

  fun blockSpan(message: BlockedSubscriptionMessageDTO) {
    log.info("Blocking '{}', '{}' span. reason:", message.spanId, message.traceId, message.blockedDetails.reason)
    val at = timeMachine.now()
    repository.addStatusUpdate(message.spanId, blocked(at))
      .switchIfEmpty {
        val span = Span.Builder()
          .message(message)
          .status(SpanStatus.BLOCKED)
          .time(at)
          .build()

        saveOrFetch(span)
      }
      .subscribe { logSpanStatus(it) }
  }

  private fun logSpanStatus(it: Span) {
    log.debug("'{}', '{}' Span was updated to: '{}'", it.spanId, it.traceId, it.lastStatus)
  }

  fun updateWithServerError(message: PublisherResponseErrorMessage) {
    log.info("Updating span '{}', '{}' with server error", message.spanId, message.traceId, message.response.status)
    val time = timeMachine.now()

    val response = SpanResult.Builder()
      .time(time)
      .message(message)
      .build()

    if(retryableErrorSelector.accept(message)) {
      repository.updateWithResponse(message.spanId, response)
        .subscribe { log.info("'{}', '{}' Span was updated with server response: '{}'", it.spanId, it.traceId, it.latestResult) }
    } else {
      repository.responseStatusUpdate(message.spanId, notOk(time), response)
        .subscribe { log.info("'{}', '{}' Span was updated with other error response: '{}'", it.spanId, it.traceId, it.latestResult?.statusCode) }
    }
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

    repository.responseStatusUpdate(message.spanId, notOk(time), response)
      .subscribe { log.info("'{}', '{}' Span was updated with other error response: '{}'", it.spanId, it.traceId, it.latestResult?.statusCode) }
  }

  fun updateWithSuccessResponse(message: PublisherSuccessMessage) {
    log.info("Updating span '{}', '{}' with SUCCESS", message.spanId, message.traceId, message.response.status)
    val time = timeMachine.now()

    val response = SpanResult.Builder()
      .time(time)
      .message(message)
      .build()

    repository.responseStatusUpdate(message.spanId, ok(time), response)
      .subscribe { log.debug("'{}', '{}' Span was updated with server response: '{}'", it.spanId, it.traceId, it.latestResult?.statusCode) }
  }

  private fun markAsRetrying(message: Message<SignableSubscriptionMessage>) {
    val payload = message.payload
    log.info("Marking  '{}', '{}' as Retrying. ", payload.spanId, payload.traceId)
    val time = timeMachine.now()
      .plusSeconds(payload.delay.seconds)
    val details = factory.calculateSpanSendDetails(message)
    val retry = SpanRetry(time, payload.totalNumberOfTries, payload.numberOfRetries, details.t2, details.t1)
    repository.retryStatusUpdate(payload.spanId, retryingSpan(time), retry)
      .subscribe { logSpanStatus(it) }
  }

  private fun addRetry(message: SignableSubscriptionMessage) {
    log.info("Delaying '{}', '{}' span for '{}' seconds", message.spanId, message.traceId, message.delay.seconds)
    val time = timeMachine.now().plusSeconds(message.delay.seconds)
    val retry = SpanRetry(time, message.totalNumberOfTries, message.numberOfRetries, SENT_BY_WEBHOOKIE, SpanSendReason.RETRY)
    repository.addRetry(message.spanId, retry)
      .subscribe { logSpanStatus(it) }
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

  @PreAuthorize("hasAnyAuthority('$ROLE_CONSUMER')")
  fun userSpans(pageable: Pageable, request: SpanRequest): Flux<Span> {
    return subscriptionServiceDelegate.userApplications()
      .map { it.applicationId }
      .collectList()
      .flatMapMany {
        log.info("Fetching all spans by applications: '{}'", it)
        repository.userSpans(it, request, pageable)
      }
  }

  @PreAuthorize("hasAnyAuthority('$ROLE_CONSUMER', '$ROLE_ADMIN')")
  fun traceSpans(pageable: Pageable, traceId: String, request: TraceRequest): Flux<Span> {
    return traceRepository.findByTraceIdVerifyingReadAccess(traceId)
      .flatMap { webhookServiceDelegate.providerTopicsConsideringAdmin() }
      .flatMapMany { repository.traceSpans(pageable, traceId, it.topics, it.isAdmin, request) }
  }

  fun fetchSpanVerifyingReadAccess(spanId: String): Mono<Span> {
    return repository.findBySpanIdVerifyingReadAccess(spanId)
  }

  fun resend(spanIds: List<String>): Mono<List<String>> {
    return spanIds.toFlux()
      .flatMap { createResendMessage(it)}
      .doOnNext { resendSpanChannel.send(it) }
      .map { it.payload.spanId }
      .collectList()
      .switchIfEmpty(emptyList<String>().toMono())
  }

  private fun createResendMessage(spanId: String): Mono<Message<ResendSpanMessage>> {
    return repository.spanByIdAndStatus(spanId, listOf(SpanStatus.OK, SpanStatus.NOT_OK))
      .zipWhen { traceRepository.findByTraceIdVerifyingReadAccess(it.traceId) }
      .zipWith(securityHandler.data())
      .map {
        val payload = ResendSpanMessage.create(it.t1.t1, it.t1.t2, it.t2.email)

        val builder = MessageBuilder.withPayload(payload)

        builder.setHeader(WH_HEADER_TOPIC, payload.consumerMessage.topic)
        builder.setHeader(WH_HEADER_TRACE_ID, payload.consumerMessage.traceId )
        builder.setHeader(WH_HEADER_SPAN_ID, payload.spanId )
        builder.setHeader(WH_HEADER_RESENT, true.toString() )
        builder.setHeader(WH_HEADER_REQUESTED_BY, payload.requestedBy )
        builder.setHeader(HEADER_CONTENT_TYPE, payload.consumerMessage.contentType )

        builder.build()
      }
  }
}
