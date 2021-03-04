package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Queries.Companion.bySpanId
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
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
}
