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

import com.hookiesolutions.webhookie.audit.domain.Trace
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.byTraceId
import com.hookiesolutions.webhookie.audit.domain.TraceRepository
import com.hookiesolutions.webhookie.audit.domain.TraceStatus
import com.hookiesolutions.webhookie.audit.domain.TraceStatusUpdate
import com.hookiesolutions.webhookie.audit.domain.TraceSummary
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.WebhookieMessage
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.StatusCountRow
import com.hookiesolutions.webhookie.common.model.TimedResult
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.webhook.service.WebhookApiServiceDelegate
import org.slf4j.Logger
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/3/21 19:06
 */
@Service
class TraceService(
  private val repository: TraceRepository,
  private val timeMachine: TimeMachine,
  private val webhookServiceDelegate: WebhookApiServiceDelegate,
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

  @PreAuthorize("hasAnyAuthority('$ROLE_PROVIDER', '$ROLE_ADMIN')")
  fun userTraces(pageable: Pageable, request: TraceRequest): Flux<Trace> {
    return webhookServiceDelegate.providerTopicsConsideringAdmin()
      .flatMapMany {
        log.info("Fetching all traces by topics: '{}'", it)
        repository.userTraces(it.topics, request, it.isAdmin, pageable)
      }
  }

  fun fetchTraceVerifyingReadAccess(traceId: String): Mono<Trace> {
    return repository.findByTraceIdVerifyingReadAccess(traceId)
  }

  fun traceIdExists(traceId: String): Mono<Boolean> {
    return repository.exists(byTraceId(traceId))
  }

  fun traceSummaryBetween(from: Instant, to: Instant): Mono<TimedResult<List<StatusCountRow>>> {
    return repository.timedCountEntitiesGroupByCreatedBetween(
      from,
      to,
      Trace.Keys.KEY_STATUS_UPDATE,
      TraceStatusUpdate.Keys.KEY_STATUS
    )
  }
}
