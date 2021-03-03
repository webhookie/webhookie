package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Traffic.Queries.Companion.byTraceId
import com.hookiesolutions.webhookie.audit.domain.Traffic.Updates.Companion.update
import com.hookiesolutions.webhookie.common.model.AbstractDocument.Keys.Companion.KEY_LAST_MODIFIED_DATE
import com.hookiesolutions.webhookie.common.model.AbstractDocument.Keys.Companion.KEY_VERSION
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
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
class TrafficRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
) : GenericRepository<Traffic>(mongoTemplate, Traffic::class.java) {
  fun findByTraceId(traceId: String): Mono<Traffic> {
    return mongoTemplate.findOne(query(byTraceId(traceId)), Traffic::class.java)
  }

  fun updateWithNoSubscription(traceId: String, status: TrafficStatus, at: Instant): Mono<Traffic> {
    val update =
      update(TrafficStatusUpdate(status, at))
    update
      .set(KEY_LAST_MODIFIED_DATE, at)
      .inc(KEY_VERSION)
    return mongoTemplate
      .findAndModify(
        query(byTraceId(traceId)),
        update,
        FindAndModifyOptions.options().returnNew(true),
        Traffic::class.java
      )
  }
}
