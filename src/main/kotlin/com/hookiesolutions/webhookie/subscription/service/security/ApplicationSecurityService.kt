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

package com.hookiesolutions.webhookie.subscription.service.security

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.webhook.service.WebhookApiServiceDelegate
import org.slf4j.Logger
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 29/1/21 17:35
 */
@Component
class ApplicationSecurityService(
  private val securityHandler: SecurityHandler,
  private val applicationAccessVoter: ApplicationAccessVoter,
  private val applicationRepository: ApplicationRepository,
  private val webhookServiceDelegate: WebhookApiServiceDelegate,
  private val log: Logger
) {
  fun verifyAccess(applicationMono: Mono<Application>): Mono<Application> {
    return Mono.zip(applicationMono, securityHandler.data())
      .filter { applicationAccessVoter.vote(it.t1, it.t2.entity, it.t2.groups) }
      .map { it.t1 }
      .switchIfEmpty { AccessDeniedException("Access Denied!").toMono() }
  }

  fun verifyReadAccess(applicationMono: Mono<Application>): Mono<Application> {
    val mono = applicationMono
      .doOnNext {
        if (log.isDebugEnabled) {
          log.debug("Verifying Application '{}' Read Access...", it.name)
        }
      }

    return verifyAccess(mono)
      .doOnError {
        log.warn("Application Read Access is Denied for the current user")
      }
  }

  fun verifyWriteAccess(applicationMono: Mono<Application>): Mono<Application> {
    val mono = applicationMono
      .doOnNext {
        if (log.isDebugEnabled) {
          log.debug("Verifying Application '{}' WRITE Access...", it.name)
        }
      }

    return verifyAccess(mono)
      .doOnError {
        log.warn("Application Write Access is Denied for the current user")
      }
  }

  fun verifySubscriptionReadAccess(subscriptionMono: Mono<Subscription>): Mono<Subscription> {
    val applicationMono = subscriptionMono
        .doOnNext { log.debug("Verifying Subscription READ access: '{}', application: '{}'", it.id, it.application.name) }
        .zipWhen { applicationRepository.findById(it.application.applicationId) }
        .map { it.t2 }

    return verifyReadAccess(applicationMono)
      .flatMap { subscriptionMono }
  }

  fun verifySubscriptionWriteAccess(subscriptionMono: Mono<Subscription>): Mono<Subscription> {
    val applicationMono = subscriptionMono
        .doOnNext { log.debug("Verifying Subscription WRITE access: '{}', application: '{}'", it.id, it.application.name) }
        .zipWhen { applicationRepository.findById(it.application.applicationId) }
        .map { it.t2 }

    return verifyWriteAccess(applicationMono)
      .flatMap { subscriptionMono }
  }

  fun verifySubscriptionProviderAccess(subscriptionMono: Mono<Subscription>): Mono<Subscription> {
    return Mono.zip(webhookServiceDelegate.providerTopics(), subscriptionMono)
      .doOnNext { log.debug("Checking Provider Access (topics: '{}') to subscription topic: '{}'", it.t1.size, it.t2.topic) }
      .flatMap {
        return@flatMap if(it.t1.contains(it.t2.topic)) {
          subscriptionMono
        } else {
          Mono.error(AccessDeniedException("Insufficient access rights to suspend subscription to '${it.t2.topic}'"))
        }
      }
      .doOnError { log.error(it.localizedMessage) }
  }
}
