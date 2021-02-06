package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.subscription.domain.Callback.Queries.Companion.applicationIdIs
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/2/21 01:33
 */
@Repository
class CallbackRepository(
  private val mongoTemplate: ReactiveMongoTemplate
): GenericRepository<Callback>(mongoTemplate, Callback::class.java) {
  fun findApplicationCallbacks(applicationId: String): Flux<Callback> {
    return mongoTemplate
      .find(
        query(applicationIdIs(applicationId)),
        Callback::class.java
      )
  }
}