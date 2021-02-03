package com.hookiesolutions.webhookie.subscription.config

import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Callback
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import org.slf4j.Logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:33
 */
@Configuration
class SubscriptionMongoConfig(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val mongoMappingContext: MongoMappingContext,
  private val logger: Logger
) {
  @EventListener(ApplicationReadyEvent::class)
  fun initIndicesAfterStartup() {
    val resolver = MongoPersistentEntityIndexResolver(mongoMappingContext)

    Flux.just(
      BlockedSubscriptionMessage::class.java,
      Application::class.java,
      Callback::class.java,
      Subscription::class.java
    )
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
      }
      .subscribe { name -> logger.info("ensureIndex: '{}'", name) }
  }
}