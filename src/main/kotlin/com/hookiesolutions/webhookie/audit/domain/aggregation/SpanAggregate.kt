package com.hookiesolutions.webhookie.audit.domain.aggregation

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.Trace
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 23/3/21 12:05
 */
@Service
class SpanAggregate: TraceAggregationStrategy {
  override val clazz: Class<out AbstractEntity> = Span::class.java

  override fun aggregate(spanCriteria: Criteria, traceCriteria: Criteria): Aggregation {
    val asField = "trace"
    val distinctTraceAlias = "y"
    return Aggregation.newAggregation(
      Aggregation.match(spanCriteria),
      Aggregation.lookup(Trace.Keys.TRACE_COLLECTION_NAME, Span.Keys.KEY_TRACE_ID, Trace.Keys.KEY_TRACE_ID, asField),
      Aggregation.project(asField).andExclude(Fields.UNDERSCORE_ID),
      Aggregation.unwind(GenericRepository.mongoField(asField)),
      Aggregation.replaceRoot(GenericRepository.mongoField(asField)),
      Aggregation.match(traceCriteria),
      Aggregation.group(Fields.UNDERSCORE_ID).first(AbstractEntity.Keys.AGGREGATE_ROOT_FIELD).`as`(distinctTraceAlias),
      Aggregation.replaceRoot(GenericRepository.mongoField(distinctTraceAlias)),
    )
  }
}
