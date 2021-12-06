package com.hookiesolutions.webhookie.instance.migration.versions.v110

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.instance.migration.VersionMigration
import com.hookiesolutions.webhookie.subscription.service.converter.CallbackSecretConverter
import org.bson.types.Binary
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VersionMigrationV110(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val converter: SecretConverter,
  private val encoder: CallbackSecretConverter,
) : VersionMigration {
  override val toVersion: String
    get() = "1.1.0"

  override fun doMigrate(): Mono<String> {
    val collection = "callback"
    val subscriptionCollection = "subscription"
    return mongoTemplate
      .find<Map<String, Any>>(
        Query.query(Criteria.where("security").exists(true)),
        collection
      )
      .flatMap {
        val security: Map<*, *> = it["security"] as Map<*, *>
        val secret = security["secret"] as Binary
        val data = encoder.encode(converter.convert(secret))
        val id = it["_id"] as ObjectId

        mongoTemplate
          .updateFirst(
            Query.query(AbstractEntity.Queries.byId(id.toHexString())),
            Update()
              .set("securityScheme", data)
              .unset("security"),
            collection
          )
          .then(
            mongoTemplate
              .updateMulti(
                Query.query(Criteria.where("callback.callbackId").`is`(id.toHexString())),
                Update()
                  .set("callback.securityScheme", data)
                  .unset("callback.security"),
                subscriptionCollection
              )
          )
      }
      .collectList()
      .map { toVersion }
  }
}
