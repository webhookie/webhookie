/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

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
