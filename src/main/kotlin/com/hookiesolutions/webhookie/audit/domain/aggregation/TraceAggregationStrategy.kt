package com.hookiesolutions.webhookie.audit.domain.aggregation

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 23/3/21 12:00
 */
interface TraceAggregationStrategy {
  fun aggregate(spanCriteria: Criteria, traceCriteria: Criteria): Aggregation
  val clazz: Class<out AbstractEntity>
}
