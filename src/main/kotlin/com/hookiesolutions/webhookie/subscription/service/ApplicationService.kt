package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.common.service.AdminServiceDelegate
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
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
  private val adminServiceDelegate: AdminServiceDelegate,
  private val securityHandler: SecurityHandler,
  private val repository: ApplicationRepository,
) {
  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun createApplication(body: ApplicationRequest): Mono<Application> {
    return adminServiceDelegate.verifyConsumerGroups(body.consumerGroups)
      .flatMap { securityHandler.entity() }
      .map { factory.createApplicationRequestToApplication(body, it) }
      .flatMap { repository.save(it) }
      .doOnNext { log.info("Application '{}' was created successfully", it.name) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun userApplications(): Flux<Application> {
    return securityHandler.entity()
      .zipWith(securityHandler.groups())
      .doOnNext { log.info("Fetching all applications for entity: '{}'", it) }
      .flatMapMany { repository.userApplications(it.t1, it.t2) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun applicationById(id: String): Mono<Application> {
    log.info("Fetching Application by id: '{}'", id)
    return repository.findByIdVerifyingReadAccess(id)
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun deleteApplication(id: String): Mono<String> {
    return repository.findByIdVerifyingWriteAccess(id)
      .map { DeletableEntity(it, true) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun updateApplication(id: String, request: ApplicationRequest): Mono<Application> {
    return repository.findByIdVerifyingWriteAccess(id)
      .zipWhen { adminServiceDelegate.verifyConsumerGroups(request.consumerGroups) }
      .map { it.t1.copy(name = request.name, consumerIAMGroups = request.consumerGroups) }
      .map { UpdatableEntity(it, true) }
      .flatMap { repository.update(it, id) }
  }
}