package com.hookiesolutions.webhookie.instance.migration

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class MigrationHealthIndicator(
  private val migrator: Migrator
): ReactiveHealthIndicator {
  override fun health(): Mono<Health> {
    return if(migrator.isDone()) {
      Health.up()
        .withDetail("Previous Version", migrator.previousVersion())
        .withDetail("Current Version", migrator.currentVersion())
        .build()
        .toMono()
    } else {
      Health.down()
        .withDetail("migration", "Data Migration is in progress...")
        .build()
        .toMono()
    }
  }
}
