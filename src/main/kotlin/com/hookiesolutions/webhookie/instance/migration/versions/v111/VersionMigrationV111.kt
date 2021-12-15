package com.hookiesolutions.webhookie.instance.migration.versions.v111

import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.mongoField
import com.hookiesolutions.webhookie.instance.migration.VersionMigration
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import org.slf4j.Logger
import org.springframework.data.mongodb.core.aggregation.SetOperation
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VersionMigrationV111(
  private val log: Logger,
  private val applicationRepository: ApplicationRepository
) : VersionMigration {
  override val toVersion: String
    get() = "1.1.1"

  override fun doMigrate(): Mono<String> {
    val operation = SetOperation
      .set("managers")
      .toValueOf(listOf(mongoField("createdBy")))
    return applicationRepository.aggregationUpdateAll(Application::class.java, operation)
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
