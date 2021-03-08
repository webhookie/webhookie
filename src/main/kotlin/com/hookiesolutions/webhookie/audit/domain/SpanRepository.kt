package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_LAST_RETRY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_RETRY_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_LAST_STATUS
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.bySpanId
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.aggregation.SetOperation
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
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

  fun addStatusUpdate(spanId: String, spanStatusUpdate: SpanStatusUpdate, retry: SpanRetry? = null): Mono<Span> {
    val updateAsArrayKey = "updateAsArray"
    val retryAsArrayKey = "retryAsArray"

    val updatePipeline = mutableListOf<AggregationOperation>()

    updatePipeline.add(mongoObjectToArray(updateAsArrayKey, spanStatusUpdate))

    val newUpdates = ArrayOperators.ConcatArrays
      .arrayOf(mongoField(KEY_STATUS_HISTORY))
      .concat(mongoField(updateAsArrayKey))
    updatePipeline.add(mongoSet(KEY_STATUS_HISTORY, newUpdates))
    updatePipeline.add(mongoSet(KEY_LAST_STATUS, spanStatusUpdate))

    if (retry != null) {
      val retryHistoryField = mongoField(KEY_RETRY_HISTORY)
      val lteFilter = ArrayOperators.Filter
        .filter(retryHistoryField)
        .`as`("r")
        .by(ComparisonOperators.Lte.valueOf("${'$'}${'$'}r.no").lessThanEqualToValue(retry.no))
      val gtFilter = ArrayOperators.Filter
        .filter(retryHistoryField)
        .`as`("r")
        .by(ComparisonOperators.Gt.valueOf("${'$'}${'$'}r.no").greaterThanValue(retry.no))

      updatePipeline.add(mongoObjectToArray(retryAsArrayKey, retry))

      val newRetries = ArrayOperators.ConcatArrays
        .arrayOf(lteFilter)
        .concat(mongoField(retryAsArrayKey))
        .concat(gtFilter)


      updatePipeline.add(mongoSet(KEY_RETRY_HISTORY, newRetries))
      val setLastRetry = SetOperation.set(KEY_LAST_RETRY)
        .toValueOf(ArrayOperators.ArrayElemAt.arrayOf(retryHistoryField).elementAt(-1))
      updatePipeline.add(setLastRetry)
    }

    updatePipeline.add(mongoUnset(retryAsArrayKey, updateAsArrayKey))

    val update = AggregationUpdate.newUpdate(*updatePipeline.toTypedArray())

    return mongoTemplate
      .findAndModify(
        query(bySpanId(spanId)),
        update,
        Span::class.java
      )
  }
}
