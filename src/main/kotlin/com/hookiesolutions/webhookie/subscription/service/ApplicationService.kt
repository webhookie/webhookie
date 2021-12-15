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

package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.UpdatableEntity.Companion.updatable
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionRepository
import com.hookiesolutions.webhookie.subscription.service.factory.ConversionsFactory
import com.hookiesolutions.webhookie.subscription.service.model.ApplicationRequest
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 22:42
 */
@Service
class ApplicationService(
  private val log: Logger,
  private val factory: ConversionsFactory,
  private val securityHandler: SecurityHandler,
  private val subscriptionRepository: SubscriptionRepository,
  private val repository: ApplicationRepository,
) {
  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun createApplication(body: ApplicationRequest): Mono<Application> {
    return securityHandler.data()
      .map { factory.createApplicationRequestToApplication(body, it.entity, it.email) }
      .flatMap { repository.save(it) }
      .doOnNext { log.info("Application '{}' was created successfully", it.name) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun userApplications(): Flux<Application> {
    return securityHandler.data()
      .doOnNext { log.info("Fetching all applications for entity: '{}'", it) }
      .flatMapMany { repository.userApplications(it.entity, it.email) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun applicationById(id: String): Mono<Application> {
    log.info("Fetching Application by id: '{}'", id)
    return repository.findByIdVerifyingReadAccess(id)
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun deleteApplication(id: String): Mono<String> {
    return repository.findByIdVerifyingWriteAccess(id)
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun updateApplication(id: String, request: ApplicationRequest): Mono<Application> {
    return securityHandler.data()
      .zipWhen { repository.findByIdVerifyingWriteAccess(id) }
      .map { it.t2.copy(name = request.name, description = request.description) }
      .map { updatable(it) }
      .flatMap { repository.update(it, id) }
      .zipWhen {
        log.info("Updating all Application '{}' Subscriptions", it.name)
        subscriptionRepository.updateApplicationSubscriptions(id, it.details())
      }
      .doOnNext { log.info("Application '{}' was updated successfully", it.t1.name) }
      .map { it.t1 }
  }
}
