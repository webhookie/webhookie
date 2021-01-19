package com.hookiesolutions.webhookie.portal.domain.webhook

import com.hookiesolutions.webhookie.portal.domain.group.AccessGroup
import com.hookiesolutions.webhookie.portal.domain.group.AccessGroup.Queries.Companion.iamGroupNameIn
import com.hookiesolutions.webhookie.portal.domain.webhook.WebhookGroup.Queries.Companion.accessibleForProviderWith
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 16:42
 */
@Repository
class WebhookRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val repository: WebhookGroupRepository,
  private val log: Logger
) {
  fun fetchConsumerGroups(groups: List<String>, clazz: Class<out AccessGroup>): Mono<List<String>> {
    return mongoTemplate.find(query(iamGroupNameIn(groups)), clazz)
      .map { it.iamGroupName }
      .collectList()
      .flatMap {
        val notExistingGroups = groups.minus(it)
        return@flatMap if (notExistingGroups.isEmpty()) {
          it.toMono()
        } else {
          val error = "{ $notExistingGroups } could not be found in ${clazz.simpleName}s!"
          log.error(error)
          Mono.error(IllegalArgumentException(error))
        }
      }
  }

  fun save(group: WebhookGroup): Mono<WebhookGroup> {
    return repository.save(group)
  }

  fun findProviderWebhookGroups(myGroups: List<String>): Flux<WebhookGroup> {
    return mongoTemplate.find(
      query(accessibleForProviderWith(myGroups)),
      WebhookGroup::class.java
    )
  }
}