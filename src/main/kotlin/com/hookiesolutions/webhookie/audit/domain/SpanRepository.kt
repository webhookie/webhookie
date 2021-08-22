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

package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_LAST_STATUS
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_LATEST_RESULT
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_NEXT_RETRY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_RETRY_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_APPLICATION_ID
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_APPLICATION_NAME
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_CALLBACK_ID
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_CALLBACK_NAME
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_ENTITY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_ID
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_TOPIC
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_TOTAL_NUMBER_OF_TRIES
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_TRACE_ID
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.applicationsIn
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.bySpanId
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.byTraceId
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.spanIsAfter
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.spanIsBefore
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.spanTopicIn
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.statusIn
import com.hookiesolutions.webhookie.audit.domain.SpanRetry.Companion.KEY_RETRY_NO
import com.hookiesolutions.webhookie.audit.domain.SpanRetry.Companion.KEY_RETRY_STATUS_CODE
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate.Keys.Companion.KEY_TIME
import com.hookiesolutions.webhookie.audit.service.security.VerifySpanReadAccess
import com.hookiesolutions.webhookie.audit.web.model.request.SpanRequest
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.FieldMatchingStrategy
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Query.Companion.pageableWith
import org.slf4j.Logger
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import reactor.util.retry.Retry
import java.time.Duration
import java.util.Objects

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/3/21 12:39
 */
@Repository
class SpanRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val log: Logger
) : GenericRepository<Span>(mongoTemplate, Span::class.java) {
  fun findBySpanId(spanId: String): Mono<Span> {
    return mongoTemplate.findOne(query(bySpanId(spanId)), Span::class.java)
      .switchIfEmpty(EntityNotFoundException("Span not found by id: '$spanId'").toMono())
  }

  @VerifySpanReadAccess
  fun findBySpanIdVerifyingReadAccess(spanId: String): Mono<Span> {
    return findBySpanId(spanId)
  }

  @VerifySpanReadAccess
  fun spanByIdAndStatus(spanId: String, statusList: List<SpanStatus>): Mono<Span> {
    return mongoTemplate.findOne(
      query(Criteria().andOperator(bySpanId(spanId), statusIn(statusList))),
      Span::class.java
    )
  }

  private fun statusUpdateOperations(spanStatusUpdate: SpanStatusUpdate): Array<AggregationOperation> {
    val updateAsArrayKey = "updateAsArray"

    return arrayOf(
      addMongoObjectToArrayField(updateAsArrayKey, spanStatusUpdate),
      mongoSet(KEY_STATUS_HISTORY, concatArrays(KEY_STATUS_HISTORY, updateAsArrayKey)),
      mongoSet(KEY_LAST_STATUS, spanStatusUpdate),
      mongoUnset(updateAsArrayKey)
    )
  }

  fun responseStatusUpdate(
    spanId: String,
    spanStatusUpdate: SpanStatusUpdate,
    response: SpanResult
  ): Mono<Span> {
    return updateSpan(spanId, *statusUpdateOperations(spanStatusUpdate), *addResponseOperations(response))
  }

  fun retryStatusUpdate(
    spanId: String,
    spanStatusUpdate: SpanStatusUpdate,
    retry: SpanRetry
  ): Mono<Span> {
    return updateSpan(
      spanId,
      *statusUpdateOperations(spanStatusUpdate),
      *addRetryOperations(retry),
//      mongoIncOperation(KEY_TOTAL_NUMBER_OF_TRIES)
    )
  }

  fun increaseNumberOfTries(spanId: String): Mono<Span> {
    return updateSpan(
      spanId,
      mongoIncOperation(KEY_TOTAL_NUMBER_OF_TRIES)
    )
  }

  fun addStatusUpdate(
    spanId: String,
    spanStatusUpdate: SpanStatusUpdate
  ): Mono<Span> {
    return updateSpanWithoutRetry(spanId, *statusUpdateOperations(spanStatusUpdate))
  }

  fun addRetry(spanId: String, retry: SpanRetry): Mono<Span> {
    return updateSpan(spanId, *addRetryOperations(retry))
  }

  fun updateWithResponse(spanId: String, response: SpanResult): Mono<Span> {
    return updateSpan(spanId, *addResponseOperations(response))
  }

  private fun addRetryOperations(retry: SpanRetry): Array<AggregationOperation> {
    val retryAsArrayKey = "retryAsArray"

    return arrayOf(
      addMongoObjectToArrayField(retryAsArrayKey, retry),
      mongoSet(KEY_RETRY_HISTORY, insertIntoArray(KEY_RETRY_HISTORY, KEY_RETRY_NO, retryAsArrayKey, retry.no)),
      mongoSetLastElemOfArray(KEY_RETRY_HISTORY, KEY_NEXT_RETRY),
      mongoUnset(retryAsArrayKey)
    )
  }

  private fun addResponseOperations(response: SpanResult): Array<AggregationOperation> {
    val key = "tmpRetry"

    return arrayOf(
      addMongoField(key, eqFilter(KEY_RETRY_HISTORY, KEY_RETRY_NO, response.retryNo)),
      mongoSet("$key.$KEY_RETRY_STATUS_CODE", response.statusCode),
      mongoSet(KEY_RETRY_HISTORY, insertIntoArray(KEY_RETRY_HISTORY, KEY_RETRY_NO, key, response.retryNo)),
      mongoSet(KEY_LATEST_RESULT, response),
      mongoSetLastElemOfArray(KEY_RETRY_HISTORY, KEY_NEXT_RETRY),
      mongoUnset(key)
    )
  }

  private fun updateSpanWithoutRetry(spanId: String, vararg operations: AggregationOperation): Mono<Span> {
    return aggregationUpdate(bySpanId(spanId), Span::class.java, *operations)
  }

  private fun updateSpan(spanId: String, vararg operations: AggregationOperation): Mono<Span> {
    val retryBackoffSpec = Retry
      .backoff(10, Duration.ofSeconds(15))
      .doBeforeRetry { log.warn("Attempting (#{}) ({} in a row) for '{}'", it.totalRetries(), it.totalRetriesInARow(), spanId) }
      .doAfterRetry { log.debug("Retried span '{}', details: '{}'", spanId, it) }

    return updateSpanWithoutRetry(spanId, *operations)
      .switchIfEmpty { EntityNotFoundException("Span '${spanId}' is not ready yet!").toMono() }
      .retryWhen(retryBackoffSpec)
  }

  fun userSpans(
    applicationIds: List<String>,
    request: SpanRequest,
    requestedPageable: Pageable
  ): Flux<Span> {
    val pageable = pageableWith(requestedPageable, SPAN_DEFAULT_SORT, SPAN_DEFAULT_PAGE)

    val queryCriteria = mutableListOf<Criteria>()

    val requestCriteria = AbstractEntity.Queries.filters(
      KEY_TRACE_ID to (request.traceId to FieldMatchingStrategy.PARTIAL_MATCH),
      KEY_SPAN_ID to (request.spanId to FieldMatchingStrategy.PARTIAL_MATCH),
      KEY_SPAN_TOPIC to (request.topic to FieldMatchingStrategy.EXACT_MATCH),
      KEY_SPAN_APPLICATION_NAME to (request.application to FieldMatchingStrategy.PARTIAL_MATCH),
      KEY_SPAN_ENTITY to (request.entity to FieldMatchingStrategy.PARTIAL_MATCH),
      KEY_SPAN_CALLBACK_NAME to ( request.callback to FieldMatchingStrategy.PARTIAL_MATCH)
    )

    queryCriteria.add(applicationsIn(applicationIds))

    if(requestCriteria.isNotEmpty()) {
      queryCriteria.addAll(requestCriteria)
    }

    if(request.status.isNotEmpty()) {
      queryCriteria.add(statusIn(request.status))
    }

    if(Objects.nonNull(request.from)) {
      queryCriteria.add(spanIsAfter(request.from!!))
    }

    if(Objects.nonNull(request.to)) {
      queryCriteria.add(spanIsBefore(request.to!!))
    }


    val criteria = if(queryCriteria.isNotEmpty()) {
      Criteria().andOperator(*queryCriteria.toTypedArray())
    } else {
      Criteria()
    }

    val query = query(criteria).with(pageable)

    if(log.isDebugEnabled) {
      log.debug("Subscription Traffic query: '{}'", query)
    }

    return mongoTemplate.find(
      query,
      Span::class.java
    )
  }

  fun traceSpans(
    requestedPageable: Pageable,
    traceId: String,
    filterByTopics: List<String>,
    ignoreTopicsFilter: Boolean,
    request: TraceRequest
  ): Flux<Span> {
    val pageable = pageableWith(requestedPageable, SPAN_DEFAULT_SORT, SPAN_DEFAULT_PAGE)

    val requestCriteria = AbstractEntity.Queries.filters(
      KEY_SPAN_APPLICATION_ID to (request.applicationId to FieldMatchingStrategy.EXACT_MATCH),
      KEY_SPAN_ENTITY to (request.entity to FieldMatchingStrategy.EXACT_MATCH),
      KEY_SPAN_CALLBACK_ID to (request.callbackId to FieldMatchingStrategy.EXACT_MATCH)
    ).toMutableList()

    if(!ignoreTopicsFilter) {
      requestCriteria.add(spanTopicIn(filterByTopics))
    }

    var criteria = byTraceId(traceId)
    if(requestCriteria.isNotEmpty()) {
      criteria = criteria.andOperator(*requestCriteria.toTypedArray())
    }
    val query = query(criteria).with(pageable)

    if(log.isDebugEnabled) {
      log.debug("Trace spans query: '{}'", query)
    }

    return mongoTemplate.find(
      query,
      Span::class.java
    )
  }

  companion object {
    val SPAN_DEFAULT_SORT = Sort.by("$KEY_LAST_STATUS.$KEY_TIME").descending()
    val SPAN_DEFAULT_PAGE = PageRequest.of(0, 20)
  }
}
