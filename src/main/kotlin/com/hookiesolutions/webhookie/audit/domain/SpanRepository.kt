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
import com.hookiesolutions.webhookie.audit.web.model.request.SpanRequest
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.regex
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
  }

  fun addStatusUpdate(
    spanId: String,
    spanStatusUpdate: SpanStatusUpdate,
    retry: SpanRetry? = null,
    response: SpanResult? = null
  ): Mono<Span> {
    val updateAsArrayKey = "updateAsArray"

    val operations: MutableList<AggregationOperation> = mutableListOf(
      addMongoObjectToArrayField(updateAsArrayKey, spanStatusUpdate),
      mongoSet(KEY_STATUS_HISTORY, concatArrays(KEY_STATUS_HISTORY, updateAsArrayKey)),
      mongoSet(KEY_LAST_STATUS, spanStatusUpdate),
      mongoUnset(updateAsArrayKey)
    )

    if (retry != null) {
      operations.addAll(addRetryOperations(retry))
    }

    if (response != null) {
      operations.addAll(addResponseOperations(response))
    }

    return updateSpan(spanId, *operations.toTypedArray())
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

  private fun updateSpan(spanId: String, vararg operations: AggregationOperation): Mono<Span> {
    return aggregationUpdate(bySpanId(spanId), Span::class.java, *operations)
  }

  fun userSpans(
    applicationIds: List<String>,
    request: SpanRequest,
    requestedPageable: Pageable
  ): Flux<Span> {
    val pageable = pageableWith(requestedPageable, SPAN_DEFAULT_SORT, SPAN_DEFAULT_PAGE)

    val queryCriteria = mutableListOf<Criteria>()

    val requestCriteria = regex(
      KEY_TRACE_ID to request.traceId,
      KEY_SPAN_ID to request.spanId,
      KEY_SPAN_TOPIC to request.topic,
      KEY_SPAN_APPLICATION_NAME to request.application,
      KEY_SPAN_ENTITY to request.entity,
      KEY_SPAN_CALLBACK_NAME to request.callback
    )


    if(applicationIds.isNotEmpty()) {
      queryCriteria.add(applicationsIn(applicationIds))
    }

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

  fun traceSpans(requestedPageable: Pageable, traceId: String, filterByTopics: List<String>, request: TraceRequest): Flux<Span> {
    val pageable = pageableWith(requestedPageable, SPAN_DEFAULT_SORT, SPAN_DEFAULT_PAGE)

    val requestCriteria = AbstractEntity.Queries.filters(
      KEY_SPAN_APPLICATION_ID to (request.application to FieldMatchingStrategy.EXACT_MATCH),
      KEY_SPAN_ENTITY to (request.entity to FieldMatchingStrategy.EXACT_MATCH),
      KEY_SPAN_CALLBACK_ID to (request.callback to FieldMatchingStrategy.EXACT_MATCH)
    )

    val criteria = byTraceId(traceId).andOperator(spanTopicIn(filterByTopics), *requestCriteria)
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
