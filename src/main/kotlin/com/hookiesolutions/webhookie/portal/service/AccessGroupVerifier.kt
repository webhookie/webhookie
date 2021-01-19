package com.hookiesolutions.webhookie.portal.service

import com.hookiesolutions.webhookie.portal.domain.AccessGroup
import com.hookiesolutions.webhookie.portal.domain.ConsumerGroup
import com.hookiesolutions.webhookie.portal.domain.ProviderGroup
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 22:17
 */
@Service
class AccessGroupVerifier(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val log: Logger
) {
  fun verifyConsumerGroups(groups: List<String>): Mono<List<String>> {
    return verifyGroups(groups, ConsumerGroup::class.java)
  }

  fun verifyProviderGroups(groups: List<String>): Mono<List<String>> {
    return verifyGroups(groups, ProviderGroup::class.java)
  }

  private fun verifyGroups(groups: List<String>, clazz: Class<out AccessGroup>): Mono<List<String>> {
    return mongoTemplate.find(Query.query(AccessGroup.Queries.iamGroupNameIn(groups)), clazz)
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
}