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
import com.hookiesolutions.webhookie.admin.domain.AccessGroupRepository
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.common.annotation.Open
import com.hookiesolutions.webhookie.common.message.entity.EntityDeletedMessage
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.slf4j.Logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuples

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 18:32
 */
@Open
abstract class AccessGroupService<T : AccessGroup>(
  val repository: AccessGroupRepository<T>,
  val factory: AccessGroupFactory,
  val publisher: EntityEventPublisher,
  val securityHandler: SecurityHandler,
  val log: Logger,
  val clazz: Class<T>,
) {
  fun createGroup(body: SaveGroupRequest): Mono<T> {
    return factory.createAccessGroup(body, clazz).toMono()
      .flatMap { repository.save(it) }
      .doOnSuccess {
        log.info("'{}' Access Group saved successfully with id: '{}'", clazz.simpleName, it.id)
      }
      .doOnError {
        log.error(it.localizedMessage)
      }
  }

  fun allGroups(): Flux<T> {
    log.info("Fetching all '{}' Access Groups...", clazz.simpleName)
    return repository.findAll()
//      .zipWith(securityHandler.groups())
//      .filter { it.t2.contains(it.t1.iamGroupName) }
//      .map { it.t1 }
  }

  fun groupById(id: String): Mono<T> {
    log.info("Fetching '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return repository.findById(id)
  }

  fun groupByIAM(iamGroupName: String): Mono<T> {
    log.info("Fetching '{}' Access Group by iam group name: '{}'", clazz.simpleName, iamGroupName)
    return repository.findByIAMGroupName(iamGroupName)
  }

  fun deleteGroupById(id: String): Mono<String> {
    log.info("Deleting '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return repository.findById(id)
      .zipWhen { repository.delete(it) }
      .doOnNext { log.info("{} with id '{}' was deleted successfully", clazz.simpleName, id) }
      .map { Tuples.of(EntityDeletedMessage(clazz.simpleName, it.t1.iamGroupName), it.t1) }
      .doOnNext { publisher.publishDeleteEvent(it.t1, it.t2) }
      .map { "Deleted" }
  }

  fun updateGroupById(id: String, body: SaveGroupRequest): Mono<T> {
    log.info("Updating '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return repository.findById(id)
      .flatMap { repository.update(it, factory.createAccessGroup(body, clazz)) }
      .doOnNext {
        val message = EntityUpdatedMessage(it.type, it.oldValue.iamGroupName, it.newValue.iamGroupName)
        publisher.publishUpdateEvent(message, it.newValue)
      }
      .map { it.newValue }
  }
}
