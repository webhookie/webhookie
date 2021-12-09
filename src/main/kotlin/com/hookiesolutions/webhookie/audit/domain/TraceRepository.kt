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

import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.spanTopicIn
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_UPDATE
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_SUMMARY
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_TIME
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.byTraceId
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.statusIn
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.traceIdRegex
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.traceTopicIn
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.traceTopicIs
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.traceUpdatedAfter
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.traceUpdatedBefore
import com.hookiesolutions.webhookie.audit.domain.Trace.Updates.Companion.updateSummary
import com.hookiesolutions.webhookie.audit.domain.TraceStatusUpdate.Keys.Companion.KEY_STATUS
import com.hookiesolutions.webhookie.audit.domain.TraceSummary.Keys.Companion.KEY_NUMBER_OF_SPANS
import com.hookiesolutions.webhookie.audit.domain.TraceSummary.Keys.Companion.KEY_NUMBER_OF_SUCCESS
import com.hookiesolutions.webhookie.audit.domain.aggregation.TraceAggregate
import com.hookiesolutions.webhookie.audit.service.security.VerifyTraceReadAccess
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.filters
import com.hookiesolutions.webhookie.common.model.AggregationCountResult
import com.hookiesolutions.webhookie.common.model.AggregationCountResult.Companion.ZERO
import com.hookiesolutions.webhookie.common.model.FieldMatchingStrategy
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.bson.Document
import org.slf4j.Logger
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.*
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
  private val aggregateStrategy: TraceAggregate,
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
    val historyField = mongoField(KEY_STATUS_HISTORY)
    val subtract = ArithmeticOperators.Subtract
      .valueOf(ArrayOperators.arrayOf(historyField).length())
      .subtract(1)

    val value = conditionalValue(
      neExpression("$KEY_STATUS_UPDATE.$KEY_STATUS", "update.status"),
      ArrayOperators.ConcatArrays.arrayOf(mongoField(KEY_STATUS_HISTORY)).concat(mongoField("uA")),
      concatArrays("aa", "uA")
    )

    mongoSet(KEY_STATUS_HISTORY, value)
    mongoSetLastElemOfArray(KEY_STATUS_HISTORY, KEY_STATUS_UPDATE)


    val operations = arrayOf(
      AddFieldsOperation
        .addField("aSize")
        .withValue(subtract)
        .build(),
      AggregationOperation {
        Document.parse("""
          {
            ${'$'}addFields: {
              "aa": {
                ${'$'}cond: [
                  {${'$'}eq: [{"${'$'}size": "${'$'}statusHistory"}, 1]},
                  "${'$'}statusHistory",
                  {${'$'}slice: ["${'$'}statusHistory", 0, "${'$'}aSize"]}
                ]
              }
            }
          }
        """.trimIndent())
      },
      AddFieldsOperation
        .addField("update")
        .withValueOf(statusUpdate)
        .build(),
      addMongoObjectToArrayField("uA", statusUpdate),
      mongoSet(KEY_STATUS_HISTORY, value),
      mongoSetLastElemOfArray(KEY_STATUS_HISTORY, KEY_STATUS_UPDATE),
      mongoUnset("aSize", "aa", "uA", "update")
    )

    return aggregationUpdate(byTraceId(traceId), Trace::class.java, *operations)
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

    val tracesAggregation = userTracesAggregate(topics, request, ignoreTopicsFilter)
    sort(tracesAggregation, requestedPageable, SPAN_DEFAULT_SORT, SPAN_DEFAULT_PAGE)

    return mongoTemplate.aggregate(tracesAggregation, aggregateStrategy.clazz, Trace::class.java)
  }

  fun userTracesCount(
    topics: List<String>,
    request: TraceRequest,
    ignoreTopicsFilter: Boolean
  ): Mono<Long> {

    val tracesAggregation = userTracesAggregate(topics, request, ignoreTopicsFilter)
    tracesAggregation.pipeline.add(Aggregation.count().`as`("count"))

    return mongoTemplate
      .aggregate(tracesAggregation, aggregateStrategy.clazz, AggregationCountResult::class.java)
      .last(ZERO)
      .map { it.count }
  }

  private fun userTracesAggregate(
    topics: List<String>,
    request: TraceRequest,
    ignoreTopicsFilter: Boolean
  ): Aggregation {
    val requestCriteria = filters(
      Span.Keys.KEY_SPAN_APPLICATION_ID to (request.applicationId to FieldMatchingStrategy.EXACT_MATCH),
      Span.Keys.KEY_SPAN_SUBSCRIPTION_ID to (request.subscriptionId to FieldMatchingStrategy.EXACT_MATCH),
      Span.Keys.KEY_SPAN_ENTITY to (request.entity to FieldMatchingStrategy.EXACT_MATCH),
      Span.Keys.KEY_SPAN_CALLBACK_ID to (request.callbackId to FieldMatchingStrategy.EXACT_MATCH)
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

    if(request.topic != null) {
      traceRequestCriteria.add(traceTopicIs(request.topic))
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

    if(log.isDebugEnabled) {
      log.debug("Webhook Traffic Aggregation query: '{}'", tracesAggregation)
    }

    return tracesAggregation
  }

  companion object {
    val SPAN_DEFAULT_SORT = Sort.by("$KEY_STATUS_UPDATE.$KEY_TIME").descending()
    val SPAN_DEFAULT_PAGE = PageRequest.of(0, 20)
  }
}
