package com.hookiesolutions.webhookie.instance.migration.versions.v108

import com.hookiesolutions.webhookie.instance.migration.VersionMigration
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.callback.Callback
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.indexOps
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.function.BiConsumer

@Component
class VersionMigrationV108(
  private val mongoTemplate: ReactiveMongoTemplate
): VersionMigration {
  override val toVersion: String
    get() = "1.0.8"

  override fun doMigrate(): Mono<String> {
    return mongoTemplate.indexOps<Callback>().dropIndex("callback_request_target")
      .then(mongoTemplate.indexOps<Application>().dropIndex("application.name"))
      .map {
        toVersion
      }
      .onErrorResume { toVersion.toMono() }
  }
}
