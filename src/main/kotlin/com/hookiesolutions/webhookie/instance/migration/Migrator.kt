package com.hookiesolutions.webhookie.instance.migration

import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.instance.migration.domain.Migration
import com.hookiesolutions.webhookie.instance.migration.domain.MigrationHistory
import org.slf4j.Logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.atomic.AtomicBoolean

@Component
class Migrator(
  private val log: Logger,
  private val migrations: List<VersionMigration>,
  private val timeMachine: TimeMachine,
  private val mongoTemplate: ReactiveMongoTemplate
) {
  private val finished = AtomicBoolean(false)

  @Order(Ordered.HIGHEST_PRECEDENCE)
  @EventListener(ApplicationReadyEvent::class)
  fun migrate() {
    mongoTemplate
      .findOne(query(Criteria()), Migration::class.java)
      .map { it.dbVersion }
      .switchIfEmpty("1.0.0".toMono())
      .flatMap { ver ->
        migrations
          .sortedBy { it.toVersion }
          .filter { it.toVersion > ver }
          .map { it.migrate() }
          .fold(Mono.just(UP_TP_DATE)) { acc, migration ->
            acc.then(updateWith(migration))
          }
      }
      .subscribe {
        if (it == UP_TP_DATE) {
          log.info("DB version is {}", it)
        } else {
          log.info("Migration to '{}' has been completed!", it)
        }
        done()
      }
  }

  private fun updateWith(versionMono: Mono<String>): Mono<String> {
    val doneAt = timeMachine.now()
    return versionMono
      .flatMap {
        log.info("DB has been migrated to version: '{}' at '{}'", it, doneAt)
        mongoTemplate.findAndModify(
          query(Criteria()),
          Update()
            .set("dbVersion", it)
            .set("migratedAt", doneAt)
            .addToSet("history", MigrationHistory(it, doneAt)),
          FindAndModifyOptions.options().returnNew(true).upsert(true),
          Migration::class.java
        )
      }
      .map { it.dbVersion }
  }

  fun done() {
    finished.set(true)
  }

  fun isDone(): Boolean {
    return finished.get()
  }

  companion object {
    const val UP_TP_DATE = "UP-TO-DATE"
  }
}
