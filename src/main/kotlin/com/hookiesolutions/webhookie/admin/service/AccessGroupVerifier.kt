package com.hookiesolutions.webhookie.admin.service

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.domain.AccessGroup.Queries.Companion.iamGroupNameIn
import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.security.access.AccessDeniedException
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
  private val securityHandler: SecurityHandler,
  private val log: Logger
) {
  fun verifyConsumerGroups(groups: Set<String>): Mono<Set<String>> {
    return verifyGroups(groups, ConsumerGroup::class.java)
  }

  fun verifyProviderGroups(groups: Set<String>): Mono<Set<String>> {
    return securityHandler.groups()
      .flatMap {
        return@flatMap if(it.containsAll(groups)) {
          verifyGroups(groups, ProviderGroup::class.java)
        } else {
          Mono.error(AccessDeniedException("Provider access group denied: ${groups.minus(it)}"))
        }
      }
  }

  private fun verifyGroups(groups: Set<String>, clazz: Class<out AccessGroup>): Mono<Set<String>> {
    return mongoTemplate.find(query(iamGroupNameIn(groups)), clazz)
      .map { it.iamGroupName }
      .collectList()
      .map { it.toSet() }
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

  fun consumerGroupsIntersect(groups: Set<String>): Mono<Set<String>> {
    return mongoTemplate.findAll(ConsumerGroup::class.java)
      .map { it.iamGroupName }
      .collectList()
      .map { groups.intersect(it) }
      .map { it.toSet() }
  }
}
