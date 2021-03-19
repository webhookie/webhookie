package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_LAST_STATUS
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_LATEST_RESULT
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_NEXT_RETRY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_RETRY_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_APPLICATION
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_CALLBACK
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_ENTITY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_ID
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_TOPIC
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_TRACE_ID
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.applicationsIn
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.bySpanId
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.statusIn
import com.hookiesolutions.webhookie.audit.domain.SpanRetry.Companion.KEY_RETRY_NO
import com.hookiesolutions.webhookie.audit.domain.SpanRetry.Companion.KEY_RETRY_STATUS_CODE
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate.Keys.Companion.KEY_TIME
import com.hookiesolutions.webhookie.audit.web.model.request.SpanRequest
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.regex
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Query.Companion.pageableWith
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/3/21 12:39
 */
@Repository
class SpanRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
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

    val requestCriteria = regex(
      KEY_TRACE_ID to request.traceId,
      KEY_SPAN_ID to request.spanId,
      KEY_SPAN_TOPIC to request.topic,
      KEY_SPAN_APPLICATION to request.application,
      KEY_SPAN_ENTITY to request.entity,
      KEY_SPAN_CALLBACK to request.callback
    )

    var criteria = applicationsIn(applicationIds)

    if(requestCriteria.isNotEmpty()) {
      criteria = criteria.andOperator(*requestCriteria)
    }

    if(request.status.isNotEmpty()) {
      criteria = criteria.andOperator(statusIn(request.status))
    }
    
    return mongoTemplate.find(
      query(criteria).with(pageable),
      Span::class.java
    )
  }

  companion object {
    val SPAN_DEFAULT_SORT = Sort.by("$KEY_LAST_STATUS.$KEY_TIME").descending()
    val SPAN_DEFAULT_PAGE = PageRequest.of(0, 20)
  }
}
