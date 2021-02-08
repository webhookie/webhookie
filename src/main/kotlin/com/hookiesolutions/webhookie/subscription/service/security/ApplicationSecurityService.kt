package com.hookiesolutions.webhookie.subscription.service.security

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.Subscription
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
}