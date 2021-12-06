package com.hookiesolutions.webhookie.instance.migration.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("migration")
@TypeAlias("migration")
data class Migration(
  val dbVersion: String,
  val migratedAt: Instant,
  val history: List<MigrationHistory> = emptyList()
): AbstractEntity()

data class MigrationHistory(
  val toVersion: String,
  val finishedAt: Instant
)
