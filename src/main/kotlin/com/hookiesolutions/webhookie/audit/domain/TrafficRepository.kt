package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Traffic.Queries.Companion.byTraceId
import com.hookiesolutions.webhookie.common.repository.GenericRepository
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
class TrafficRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
): GenericRepository<Traffic>(mongoTemplate, Traffic::class.java) {
  fun findByTraceId(traceId: String): Mono<Traffic> {
    return mongoTemplate.findOne(query(byTraceId(traceId)), Traffic::class.java)
  }
}
