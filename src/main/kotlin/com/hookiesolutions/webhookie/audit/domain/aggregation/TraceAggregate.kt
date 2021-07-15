package com.hookiesolutions.webhookie.audit.domain.aggregation

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.Trace
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.stereotype.Service

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 23/3/21 12:04
 */
@Service
class TraceAggregate: TraceAggregationStrategy {
  override val clazz: Class<out AbstractEntity> = Trace::class.java

  override fun aggregate(spanCriteria: Criteria, traceCriteria: Criteria): Aggregation {
    val asField = "spans"

    val list: MutableList<AggregationOperation> = mutableListOf()

    if(!traceCriteria.criteriaObject.isEmpty()) {
      list.add(Aggregation.match(traceCriteria))
    }

    list.add(Aggregation.lookup(Span.Keys.SPAN_COLLECTION_NAME, Span.Keys.KEY_TRACE_ID, Trace.Keys.KEY_TRACE_ID, asField))

    if(!spanCriteria.criteriaObject.isEmpty()) {
      list.add(Aggregation.match(where(asField).elemMatch(spanCriteria)))
    }

    list.add(Aggregation.project().andExclude(asField))


    return Aggregation.newAggregation(list)
  }
}
