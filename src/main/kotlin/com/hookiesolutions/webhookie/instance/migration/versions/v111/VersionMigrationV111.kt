package com.hookiesolutions.webhookie.instance.migration.versions.v111

import com.hookiesolutions.webhookie.common.model.AbstractDocument
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.mongoField
import com.hookiesolutions.webhookie.instance.migration.VersionMigration
import com.hookiesolutions.webhookie.subscription.domain.Application
import org.slf4j.Logger
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
import org.springframework.data.mongodb.core.aggregation.SetOperation
import org.springframework.data.mongodb.core.aggregation.UnsetOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VersionMigrationV111(
  private val log: Logger,
  private val mongoTemplate: ReactiveMongoTemplate
) : VersionMigration {
  override val toVersion: String
    get() = "1.1.1"

  override fun doMigrate(): Mono<String> {
    val setManagers = SetOperation
      .set(Application.Keys.KEY_MANAGES)
      .toValueOf(listOf(mongoField(AbstractDocument.Keys.KEY_CREATED_BY)))
    val unsetConsumerGroups = UnsetOperation
      .unset("consumerIAMGroups")

    return mongoTemplate
      .findAndModify(
        Query.query(Criteria()),
        AggregationUpdate.newUpdate(setManagers, unsetConsumerGroups),
        FindAndModifyOptions.options().returnNew(false),
        Application::class.java
      )
      .doOnNext { log.info("All applications have been updated with the managers list") }
      .map { toVersion }
  }

/*
  @Suppress("unused")
  fun doMigrate1(): Mono<String> {
    return mongoTemplate.findAll(Application::class.java)
      .map { it.createdBy!! }
      .distinct()
      .collectList()
      .doOnNext { log.info("Creating '{}' UserGroup(s)...", it.size) }
      .flatMapMany { userIdList ->
        val groups = userIdList.map {
          UserGroup(RandomString.make(32), listOf(GroupMember(it, UserGroupRole.OWNER)))
        }
        mongoTemplate.insertAll(groups)
      }
      .collectList()
      .doOnNext {
        log.info("'{}' UserGroup(s) created successfully", it.size)
        log.info("Updating Applications", it.size)
      }
      .flatMapMany { it.toFlux() }
      .flatMap {
        mongoTemplate.updateMulti(
          query(where("createdBy").`is`(it.members.first().userId)),
          Update().set("ownersGroupId", it.id!!),
          Application.Keys.APPLICATION_COLLECTION_NAME
        )
      }
      .collectList()
      .map { toVersion }
  }
*/
}
