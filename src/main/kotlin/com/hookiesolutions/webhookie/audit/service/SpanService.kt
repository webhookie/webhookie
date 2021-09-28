/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.SpanRepository
import com.hookiesolutions.webhookie.audit.domain.SpanHttpResponse
import com.hookiesolutions.webhookie.audit.domain.SpanRetry
import com.hookiesolutions.webhookie.audit.domain.SpanRetry.Companion.SENT_BY_WEBHOOKIE
import com.hookiesolutions.webhookie.audit.domain.SpanSendReason
import com.hookiesolutions.webhookie.audit.domain.SpanStatus
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate
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
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.MissingSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.ResendSpanMessage
import com.hookiesolutions.webhookie.common.message.subscription.RetryableSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.StatusCountRow
import com.hookiesolutions.webhookie.common.model.TimedResult
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.webhook.service.WebhookApiServiceDelegate
import org.slf4j.Logger
import org.springframework.data.domain.Pageable
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
import java.time.Instant

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
  private val webhookServiceDelegate: WebhookApiServiceDelegate,
  private val factory: TrafficConversionFactory,
  private val subscriptionServiceDelegate: SubscriptionServiceDelegate,
  private val resendSpanChannel: MessageChannel,
  private val securityHandler: SecurityHandler,
  private val log: Logger
) {
  fun createSpan(message: SignableSubscriptionMessage): Mono<Span> {
    log.info("'{}', '{}' span to be saved", message.spanId, message.traceId)
    val span = Span.Builder()
      .message(message)
      .status(SpanStatus.PROCESSING)
      .time(timeMachine.now())
      .build()

    return saveOrFetch(span)
      .doOnNext { log.info("'{}', '{}' span was saved/fetched", it.spanId, it.traceId) }
  }

  fun increaseNumberOfTries(message: SignableSubscriptionMessage): Mono<Span> {
    log.info("Increasing number of tries for '{}', traceId: '{}'", message.spanId, message.traceId)
    return repository.increaseNumberOfTries(message.spanId)
      .doOnNext { log.info("NumberOfTries increased to '{}' for span '{}', '{}'", it.totalNumberOfTries, it.spanId, it.traceId) }
  }

  fun blockSpan(message: BlockedSubscriptionMessageDTO): Mono<Span> {
    log.info("Blocking '{}', '{}' span. reason:", message.spanId, message.traceId, message.blockedDetails.reason)
    val at = timeMachine.now()
    return repository.addStatusUpdate(message.spanId, blocked(at))
      .switchIfEmpty {
        val span = Span.Builder()
          .message(message)
          .status(SpanStatus.BLOCKED)
          .time(at)
          .build()

        saveOrFetch(span)
      }
      .doOnNext { logSpan(it) }
  }

  @Suppress("DuplicatedCode")
  fun updateWithNonRetryableServerError(message: PublisherResponseErrorMessage): Mono<Span> {
    log.info("Updating span '{}', '{}' with server error", message.spanId, message.traceId, message.response.status)
    val time = timeMachine.now()

    val response = SpanHttpResponse.Builder()
      .time(time)
      .message(message)
      .build()

    return repository.responseStatusUpdate(message.spanId, notOk(time), response)
      .doOnNext { log.info("'{}', '{}' Span was updated with other error response: '{}'", it.spanId, it.traceId, it.response?.statusCode) }
  }

  fun updateWithRetryableServerError(message: PublisherResponseErrorMessage): Mono<Span> {
    log.info("Updating span '{}', '{}' with RETRYABLE server error", message.spanId, message.traceId, message.response.status)
    val time = timeMachine.now()

    val response = SpanHttpResponse.Builder()
      .time(time)
      .message(message)
      .build()

    return repository.updateWithResponse(message.spanId, response)
      .doOnNext { log.info("'{}', '{}' Span was updated with server response: '{}'", it.spanId, it.traceId, it.response?.statusCode) }
  }

  @Suppress("DuplicatedCode")
  fun updateWithClientError(message: PublisherRequestErrorMessage): Mono<Span> {
    log.info("Updating span '{}', '{}' with request error", message.spanId, message.traceId, message.reason)
    val time = timeMachine.now()

    val response = SpanHttpResponse.Builder()
      .time(time)
      .message(message)
      .build()

    return repository.updateWithResponse(message.spanId, response)
      .doOnNext { log.info("'{}', '{}' Span was updated with client error response: '{}'", it.spanId, it.traceId, it.response?.statusCode) }
  }

  @Suppress("DuplicatedCode")
  fun updateWithOtherError(message: PublisherOtherErrorMessage): Mono<Span> {
    log.info("Updating span '{}', '{}' with unknown error", message.spanId, message.traceId, message.reason)
    val time = timeMachine.now()

    val response = SpanHttpResponse.Builder()
      .time(time)
      .message(message)
      .build()

    return repository.responseStatusUpdate(message.spanId, notOk(time), response)
      .doOnNext { log.info("'{}', '{}' Span was updated with other error response: '{}'", it.spanId, it.traceId, it.response?.statusCode) }
  }

  @Suppress("DuplicatedCode")
  fun updateWithSubscriptionError(message: MissingSubscriptionMessage): Mono<Span> {
    log.info("Updating span '{}', '{}' with subscription error", message.spanId, message.traceId, message.reason)
    val time = timeMachine.now()

    val response = SpanHttpResponse.Builder()
      .time(time)
      .message(message)
      .build()

    return repository.responseStatusUpdate(message.spanId, notOk(time), response)
      .doOnNext { log.info("'{}', '{}' Span was updated with subscription error response: '{}'", it.spanId, it.traceId, it.response?.statusCode) }
  }

  fun updateWithSuccessResponse(message: PublisherSuccessMessage): Mono<Span> {
    log.info("Updating span '{}', '{}' with SUCCESS", message.spanId, message.traceId, message.response.status)
    val time = timeMachine.now()

    val response = SpanHttpResponse.Builder()
      .time(time)
      .message(message)
      .build()

    return repository.responseStatusUpdate(message.spanId, ok(time), response)
      .doOnNext { log.info("'{}', '{}' Span was updated with server response: '{}'", it.spanId, it.traceId, it.response?.statusCode) }
  }

  fun markAsRetrying(message: Message<RetryableSubscriptionMessage>): Mono<Span> {
    val payload = message.payload
    log.info("Marking  '{}', '{}' as Retrying. ", payload.spanId, payload.traceId)
    val time = timeMachine.now()
      .plusSeconds(payload.delay.seconds)
    val details = factory.calculateSpanSendDetails(message)
    val retry = SpanRetry(time, payload.totalNumberOfTries, payload.numberOfRetries, details.t2, details.t1)
    return repository.retryStatusUpdate(payload.spanId, retryingSpan(time), retry)
      .doOnNext { logSpan(it) }
  }

  fun addRetry(message: RetryableSubscriptionMessage): Mono<Span> {
    log.info("Delaying '{}', '{}' span for '{}' seconds", message.spanId, message.traceId, message.delay.seconds)
    val time = timeMachine.now().plusSeconds(message.delay.seconds)
    val retry = SpanRetry(time, message.totalNumberOfTries, message.numberOfRetries, SENT_BY_WEBHOOKIE, SpanSendReason.RETRY)
    return repository.addRetry(message.spanId, retry)
      .doOnNext { logSpan(it) }
  }

  private fun saveOrFetch(span: Span): Mono<Span> {
    val spanId = span.spanId
    return repository.save(span)
      .doOnNext { log.info("'{}', '{}' Span saved successfully: '{}'", spanId, it.traceId, it.id) }
      .onErrorResume(EntityExistsException::class.java) {
        log.debug("'{}' Span already exists! fetching the existing document...", spanId)
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

  @PreAuthorize("hasAnyAuthority('$ROLE_CONSUMER')")
  fun subscriptionTrafficCount(request: SpanRequest): Mono<Long> {
    return subscriptionServiceDelegate.userApplications()
      .map { it.applicationId }
      .collectList()
      .flatMap {
        log.info("Fetching all spans by applications: '{}'", it)
        repository.userSpansCount(it, request)
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

        builder.setHeader(WH_HEADER_TOPIC, payload.originalMessage.topic)
        builder.setHeader(WH_HEADER_TRACE_ID, payload.originalMessage.traceId )
        builder.setHeader(WH_HEADER_SPAN_ID, payload.spanId )
        builder.setHeader(WH_HEADER_RESENT, true.toString() )
        builder.setHeader(WH_HEADER_REQUESTED_BY, payload.requestedBy )
        builder.setHeader(HEADER_CONTENT_TYPE, payload.originalMessage.contentType )

        builder.build()
      }
  }

  private fun logSpan(it: Span) {
    log.debug("'{}', '{}' Span was updated to: '{}'", it.spanId, it.traceId, it.lastStatus)
  }

  fun spanSummaryBetween(from: Instant, to: Instant): Mono<TimedResult<List<StatusCountRow>>> {
    return repository.timedCountEntitiesGroupByCreatedBetween(
      from,
      to,
      Span.Keys.KEY_LAST_STATUS,
      SpanStatusUpdate.Keys.KEY_STATUS
    )
  }
}
