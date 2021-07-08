package com.hookiesolutions.webhookie.common.config

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.slf4j.Logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 18:41
 */
@Configuration
class CommonMongoConfig(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val mongoMappingContext: MongoMappingContext,
  private val logger: Logger,
  private val indexEntityList: List<List<Class<out AbstractEntity>>>
) {
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @EventListener(ApplicationReadyEvent::class)
  fun initIndicesAfterStartup() {
    val resolver = MongoPersistentEntityIndexResolver(mongoMappingContext)

    indexEntityList.flatten()
      .toFlux()
      .flatMap { clazz ->
        resolver
          .resolveIndexFor(clazz)
          .toFlux()
          .flatMap { Mono.zip(mongoTemplate.indexOps(clazz).toMono(), it.toMono()) }
      }
      .flatMap {
        val indexOps = it.t1
        val def = it.t2
        indexOps.ensureIndex(def)
          .zipWith(def.toMono())
      }
      .subscribe { logger.info("ensureIndex: '{}', '{}', '{}'", it.t1, it.t2.indexKeys, it.t2.indexOptions) }
  }
}
