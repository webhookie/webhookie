package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Trace.Queries.Companion.byTraceId
import com.hookiesolutions.webhookie.audit.domain.Trace.Updates.Companion.trafficStatusUpdate
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

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
        trafficStatusUpdate(statusUpdate),
        FindAndModifyOptions.options().returnNew(true),
        Trace::class.java
      )
  }
}
