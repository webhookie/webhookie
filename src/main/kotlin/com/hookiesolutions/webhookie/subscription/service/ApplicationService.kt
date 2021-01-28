package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.service.model.CreateApplicationRequest
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
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
  private val applicationRepository: ApplicationRepository
) {
  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun createApplication(body: CreateApplicationRequest): Mono<Application> {
    return securityHandler.entity()
      .map { factory.createApplicationRequestToApplication(body, it) }
      .flatMap { applicationRepository.save(it) }
      .doOnNext { log.info("Application '{}' was created successfully", it.name) }
  }
}