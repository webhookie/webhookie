package com.hookiesolutions.webhookie.instance.migration.versions.v112

import com.hookiesolutions.webhookie.common.model.AbstractEntity.Companion.mongoField
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.all
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.idIsIn
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.fieldName
import com.hookiesolutions.webhookie.instance.migration.VersionMigration
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_CALLBACK
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.subscriptionIsActive
import com.hookiesolutions.webhookie.subscription.domain.callback.Callback
import com.hookiesolutions.webhookie.subscription.domain.callback.CallbackDetails.Keys.Companion.KEY_CALLBACK_ID
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VersionMigrationV112(
  private val log: Logger,
  private val mongoTemplate: ReactiveMongoTemplate
) : VersionMigration {
  override val toVersion: String
    get() = "1.1.2"

  override fun doMigrate(): Mono<String> {
    val agg = Aggregation.newAggregation(
      Aggregation.match(subscriptionIsActive()),
      Aggregation.project(fieldName(KEY_CALLBACK, KEY_CALLBACK_ID)).andExclude(UNDERSCORE_ID),
      Aggregation.group().addToSet(mongoField(KEY_CALLBACK_ID)).`as`(KEY_CALLBACK_ID),
      Aggregation.unwind(mongoField(KEY_CALLBACK_ID)),
      Aggregation.project(KEY_CALLBACK_ID).andExclude(UNDERSCORE_ID),
    )
    val callbackClass = Callback::class.java
    val subscriptionClass = Subscription::class.java
    return mongoTemplate.updateMulti(all(), Callback.Updates.openCallback(), callbackClass)
      .flatMap {
        mongoTemplate.aggregate(agg, subscriptionClass, Map::class.java)
          .collectList()
          .map { map -> map.map { it[KEY_CALLBACK_ID] as String } }
      }
      .flatMap { mongoTemplate.updateMulti(Query.query(idIsIn(it)), Callback.Updates.lockCallback(), callbackClass) }
      .doOnNext { log.info("All callbacks have been updated with the edit status") }
      .map { toVersion }
  }
}
