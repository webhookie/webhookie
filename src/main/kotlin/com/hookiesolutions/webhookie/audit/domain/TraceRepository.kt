package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_TOPIC
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.spanTopicIn
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_UPDATE
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_SUMMARY
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_TIME
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.byTraceId
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.statusIn
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.traceIdRegex
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.traceTopicIn
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.traceUpdatedAfter
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.traceUpdatedBefore
import com.hookiesolutions.webhookie.audit.domain.Trace.Updates.Companion.traceStatusUpdate
import com.hookiesolutions.webhookie.audit.domain.Trace.Updates.Companion.updateSummary
import com.hookiesolutions.webhookie.audit.domain.TraceSummary.Keys.Companion.KEY_NUMBER_OF_SPANS
import com.hookiesolutions.webhookie.audit.domain.TraceSummary.Keys.Companion.KEY_NUMBER_OF_SUCCESS
import com.hookiesolutions.webhookie.audit.domain.aggregation.TraceAggregationStrategy
import com.hookiesolutions.webhookie.audit.service.security.VerifyTraceReadAccess
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.filters
import com.hookiesolutions.webhookie.common.model.FieldMatchingStrategy
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/3/21 19:07
 */
@Repository
class TraceRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Qualifier("traceAggregate") private val aggregateStrategy: TraceAggregationStrategy,
  private val log: Logger
) : GenericRepository<Trace>(mongoTemplate, Trace::class.java) {
  fun findByTraceId(traceId: String): Mono<Trace> {
    return mongoTemplate.findOne(query(byTraceId(traceId)), Trace::class.java)
  }

  @VerifyTraceReadAccess
  fun findByTraceIdVerifyingReadAccess(traceId: String): Mono<Trace> {
    return findByTraceId(traceId)
  }

  fun addStatus(traceId: String, statusUpdate: TraceStatusUpdate): Mono<Trace> {
    return mongoTemplate
      .findAndModify(
        query(byTraceId(traceId)),
        traceStatusUpdate(statusUpdate),
        FindAndModifyOptions.options().returnNew(true),
        Trace::class.java
      )
  }

  fun updateWithSummary(traceId: String, summary: TraceSummary, traceStatusUpdate: TraceStatusUpdate): Mono<Trace> {
    return mongoTemplate
      .findAndModify(
        query(byTraceId(traceId)),
        updateSummary(summary, traceStatusUpdate),
        FindAndModifyOptions.options().returnNew(true),
        Trace::class.java
      )
  }

  fun increaseSuccessSpan(traceId: String, at: Instant): Mono<Trace> {
    val tempKey = "tempKey"
    val spansKey = "$KEY_SUMMARY.$KEY_NUMBER_OF_SPANS"
    val successKey = "$KEY_SUMMARY.$KEY_NUMBER_OF_SUCCESS"
    val value = conditionalValue(
      neExpression(spansKey, successKey),
      ArrayOperators.ConcatArrays.arrayOf(mongoField(KEY_STATUS_HISTORY)),
      concatArrays(KEY_STATUS_HISTORY, tempKey)
    )

    val operations = arrayOf<AggregationOperation>(
      addMongoObjectToArrayField(tempKey, TraceStatusUpdate.ok(at)),
      mongoIncOperation(successKey),
      AddFieldsOperation
        .addField(KEY_STATUS_HISTORY)
        .withValue(value)
        .build(),
      mongoSetLastElemOfArray(KEY_STATUS_HISTORY, KEY_STATUS_UPDATE),
      mongoUnset(tempKey)
    )

    return aggregationUpdate(byTraceId(traceId), Trace::class.java, *operations)
  }

  fun userTraces(
    topics: List<String>,
    request: TraceRequest,
    ignoreTopicsFilter: Boolean,
    requestedPageable: Pageable
  ): Flux<Trace> {
    val requestCriteria = filters(
      KEY_SPAN_TOPIC to (request.topic to FieldMatchingStrategy.EXACT_MATCH),
      Span.Keys.KEY_SPAN_APPLICATION_ID to (request.application to FieldMatchingStrategy.EXACT_MATCH),
      Span.Keys.KEY_SPAN_ENTITY to (request.entity to FieldMatchingStrategy.EXACT_MATCH),
      Span.Keys.KEY_SPAN_CALLBACK_ID to (request.callback to FieldMatchingStrategy.EXACT_MATCH)
    )

    var spanCriteria = if(ignoreTopicsFilter) {
      Criteria()
    } else {
      spanTopicIn(topics)
    }

    if(requestCriteria.isNotEmpty()) {
      spanCriteria = spanCriteria.andOperator(*requestCriteria)
    }
    val traceRequestCriteria = mutableListOf<Criteria>()

    if(request.traceId != null) {
      traceRequestCriteria.add(traceIdRegex(request.traceId))
    }

    if(!ignoreTopicsFilter) {
      traceRequestCriteria.add(traceTopicIn(topics))
    }

    if(request.status.isNotEmpty()) {
      traceRequestCriteria.add(statusIn(request.status))
    }

    if(Objects.nonNull(request.from)) {
      traceRequestCriteria.add(traceUpdatedAfter(request.from!!))
    }

    if(Objects.nonNull(request.to)) {
      traceRequestCriteria.add(traceUpdatedBefore(request.to!!))
    }

    val traceCriteria = if(traceRequestCriteria.isEmpty()) {
      Criteria()
    } else {
      Criteria().andOperator(*traceRequestCriteria.toTypedArray())
    }

    val tracesAggregation = aggregateStrategy.aggregate(spanCriteria, traceCriteria)
    sort(tracesAggregation, requestedPageable, SPAN_DEFAULT_SORT, SPAN_DEFAULT_PAGE)

    if(log.isDebugEnabled) {
      log.debug("Webhook Traffic Aggregation query: '{}'", tracesAggregation)
    }

    return mongoTemplate.aggregate(
      tracesAggregation,
      aggregateStrategy.clazz,
      Trace::class.java
    )
  }

  companion object {
    val SPAN_DEFAULT_SORT = Sort.by("$KEY_STATUS_UPDATE.$KEY_TIME").descending()
    val SPAN_DEFAULT_PAGE = PageRequest.of(0, 20)
  }
}
