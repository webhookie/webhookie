package com.hookiesolutions.webhookie.instance.migration.versions.v130

import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.all
import com.hookiesolutions.webhookie.instance.migration.VersionMigration
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VersionMigration130(
  private val log: Logger,
  private val mongoTemplate: ReactiveMongoTemplate
) : VersionMigration {
  override val toVersion: String
    get() = "1.3.0"

  override fun doMigrate(): Mono<String> {
    val updateWebhookApis = Update.update(WebhookApi.Keys.KEY_REQUIRES_APPROVAL, false)
    return mongoTemplate.updateMulti(all(), updateWebhookApis, WebhookApi::class.java)
      .doOnNext { log.info("All webhook APIs have been updated with approval details") }
      .map { toVersion }
  }
}
