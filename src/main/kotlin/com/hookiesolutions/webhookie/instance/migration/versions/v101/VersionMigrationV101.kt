package com.hookiesolutions.webhookie.instance.migration.versions.v101

import com.hookiesolutions.webhookie.instance.migration.VersionMigration
import org.slf4j.Logger
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class VersionMigrationV101: VersionMigration {
  override val toVersion: String
    get() = "1.0.1"

  override fun doMigrate(): Mono<String> {
    return Mono.defer { Mono.just(toVersion) }
      .delayElement(Duration.ofSeconds(5))
  }
}
