package com.hookiesolutions.webhookie.instance.migration

import org.slf4j.Logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class Migrator(
  private val log: Logger,
  private val migrations: List<Migration>,
) {
  private val finished = AtomicBoolean(false)

  @EventListener(ApplicationReadyEvent::class)
  fun migrate() {
    val ver = "0.0.1"

    migrations
      .sortedBy { it.toVersion }
      .filter { it.toVersion > ver }
      .map { it.migrate() }
      .reduce { acc, migration -> acc.then(migration) }
      .subscribe {
        log.info("Migration to '{}' has been completed!", it)
        done()
      }
  }

  fun done() {
    finished.set(true)
  }

  fun isDone(): Boolean {
    return finished.get()
  }
}
