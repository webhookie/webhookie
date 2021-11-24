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
import com.hookiesolutions.webhookie.subscription.domain.callback.Callback
import com.hookiesolutions.webhookie.subscription.domain.callback.CallbackRepository
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionRepository
import com.hookiesolutions.webhookie.subscription.service.converter.CallbackSecretConverter
import com.hookiesolutions.webhookie.subscription.service.model.CallbackRequest
import com.hookiesolutions.webhookie.subscription.service.security.annotation.ApplicationAccessType
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationAccessById
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/2/21 01:40
 */
@Service
class CallbackService(
  private val repository: CallbackRepository,
  private val converter: CallbackSecretConverter,
  private val subscriptionRepository: SubscriptionRepository,
  private val log: Logger,
) {
  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.WRITE)
  fun createCallback(applicationId: String, request: CallbackRequest): Mono<Callback> {
    log.info("adding Callback: '{}' to application: '{}'", request.requestTarget(), applicationId)
    val callback = request.callback(applicationId)
    return repository.save(callback)
      .doOnNext { log.info("Callback '{}' was created successfully", it.requestTarget()) }
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.READ)
  fun applicationCallbacks(applicationId: String): Flux<Callback> {
    log.info("fetching all application callbacks : '{}'", applicationId)
    return repository.findApplicationCallbacks(applicationId)
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.READ)
  fun applicationCallbackById(applicationId: String, id: String): Mono<Callback> {
    log.info("fetching callback : '{}'", id)
    return repository.findById(id)
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.WRITE)
  fun deleteApplicationCallbackById(applicationId: String, id: String): Mono<String> {
    log.info("deleting callback : '{}'", id)
    return repository.findById(id)
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.WRITE)
  @Transactional
  fun updateCallback(applicationId: String, id: String, body: CallbackRequest): Mono<Callback> {
    log.info("updating Callback: '{}', '{}'", body.requestTarget(), id)

    return repository.findById(id)
      .map { updatable(body.copy(it, applicationId)) }
      .flatMap { repository.update(it, id) }
      .zipWhen { callback ->
        //TODO: refactor
        log.info("Updating all Callback '{}' Subscriptions", callback.name)
        val details = converter.convert(callback)

        subscriptionRepository.updateCallbackSubscriptions(id, details)
      }
      .doOnNext { log.info("Callback '{}' was updated successfully", it.t1.requestTarget()) }
      .map { it.t1 }
  }

  @VerifyApplicationAccessById(access = ApplicationAccessType.READ)
  fun countActiveSubscriptions(applicationId: String, callbackId: String): Mono<Long> {
    log.info("reading number of subscriptions for Callback: '{}'", callbackId)
    return subscriptionRepository.countActiveSubscriptionsByCallbackId(callbackId)
  }
}
