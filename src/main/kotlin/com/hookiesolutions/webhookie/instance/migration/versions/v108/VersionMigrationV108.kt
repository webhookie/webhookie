package com.hookiesolutions.webhookie.instance.migration.versions.v108

import com.hookiesolutions.webhookie.instance.migration.VersionMigration
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class VersionMigrationV108: VersionMigration {
  override val toVersion: String
    get() = "1.0.8"

  override fun doMigrate(): Mono<String> {
    return Mono.defer { Mono.just(toVersion) }
      .delayElement(Duration.ofSeconds(8))
  }
}
