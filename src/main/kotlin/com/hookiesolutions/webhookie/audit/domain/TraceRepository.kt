package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_UPDATE
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_SUMMARY
import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.byTraceId
import com.hookiesolutions.webhookie.audit.domain.Trace.Updates.Companion.traceStatusUpdate
import com.hookiesolutions.webhookie.audit.domain.Trace.Updates.Companion.updateSummary
import com.hookiesolutions.webhookie.audit.domain.TraceSummary.Keys.Companion.KEY_NUMBER_OF_SPANS
import com.hookiesolutions.webhookie.audit.domain.TraceSummary.Keys.Companion.KEY_NUMBER_OF_SUCCESS
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/3/21 19:07
 */
@Repository
class TraceRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
) : GenericRepository<Trace>(mongoTemplate, Trace::class.java) {
  fun findByTraceId(traceId: String): Mono<Trace> {
    return mongoTemplate.findOne(query(byTraceId(traceId)), Trace::class.java)
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
      AddFieldsOperation
        .addField(successKey)
        .withValue(ArithmeticOperators.Add.valueOf(mongoField(successKey)).add(1))
        .build(),
      AddFieldsOperation
        .addField(KEY_STATUS_HISTORY)
        .withValue(value)
        .build(),
      mongoSetLastElemOfArray(KEY_STATUS_HISTORY, KEY_STATUS_UPDATE),
      mongoUnset(tempKey)
    )

    return aggregationUpdate(byTraceId(traceId), Trace::class.java, *operations)
  }
}
