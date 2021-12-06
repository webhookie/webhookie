package com.hookiesolutions.webhookie.instance.migration.versions.v108

import com.hookiesolutions.webhookie.instance.migration.Migration
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class MigrationV108 : Migration {
  override val toVersion: String
    get() = "1.0.8"

  override fun doMigrate(): Mono<String> {
    return Mono.defer { Mono.just(toVersion) }
      .delayElement(Duration.ofSeconds(8))
      .doOnNext { println(it) }
  }
}
