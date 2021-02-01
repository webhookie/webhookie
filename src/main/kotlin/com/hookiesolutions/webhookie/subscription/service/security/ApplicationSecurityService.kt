package com.hookiesolutions.webhookie.subscription.service.security

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.subscription.domain.Application
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.util.function.Supplier

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 29/1/21 17:35
 */
@Component
class ApplicationSecurityService(
  private val securityHandler: SecurityHandler,
  private val applicationAccessVoter: ApplicationAccessVoter
) {
  fun verifyReadAccess(applicationSupplier: Supplier<Mono<Application>>): Mono<Application> {
    return Mono.zip(applicationSupplier.get(), securityHandler.entity(), securityHandler.groups())
      .filter { applicationAccessVoter.vote(it.t1, it.t2, it.t3) }
      .map { it.t1 }
      .switchIfEmpty { AccessDeniedException("Access Denied!").toMono() }
  }
}