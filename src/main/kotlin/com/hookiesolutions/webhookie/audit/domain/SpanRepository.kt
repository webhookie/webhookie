package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_STATUS_UPDATE
import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.bySpanId
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.aggregation.SetOperation
import org.springframework.data.mongodb.core.aggregation.UnsetOperation
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
  private val mongoTemplate: ReactiveMongoTemplate
): GenericRepository<Span>(mongoTemplate, Span::class.java) {
  fun findBySpanId(spanId: String): Mono<Span> {
    return mongoTemplate.findOne(query(bySpanId(spanId)), Span::class.java)
  }

  fun addStatusUpdate(traceId: String, spanId: String, spanStatusUpdate: SpanStatusUpdate): Mono<Span> {
    val updateAsArrayKey = "updateAsArray"

    val newUpdates = ArrayOperators.ConcatArrays
      .arrayOf("${'$'}$KEY_STATUS_HISTORY")
      .concat("${'$'}$updateAsArrayKey")
    val update = AggregationUpdate.newUpdate(
      AddFieldsOperation
        .addField(updateAsArrayKey)
        .withValueOf(arrayOf(spanStatusUpdate))
        .build(),
      SetOperation
        .set(KEY_STATUS_HISTORY)
        .toValueOf(newUpdates),
      SetOperation
        .set(KEY_STATUS_UPDATE)
        .toValue(spanStatusUpdate),
      UnsetOperation
        .unset(updateAsArrayKey)
    )

    return mongoTemplate
      .findAndModify(
        query(bySpanId(spanId)),
        update,
        Span::class.java
      )
  }
}
