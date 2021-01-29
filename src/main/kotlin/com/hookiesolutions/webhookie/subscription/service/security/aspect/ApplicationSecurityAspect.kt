package com.hookiesolutions.webhookie.subscription.service.security.aspect

import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.service.security.ApplicationSecurityService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.Logger
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 29/1/21 17:40
 */
@Aspect
@Component
class ApplicationSecurityAspect(
  private val securityService: ApplicationSecurityService,
  private val log: Logger
) {
  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationReadAccess)")
  fun annotatedVerifyApplicationReadAccess() {
  }

  @Pointcut("execution(reactor.core.publisher.Mono<com.hookiesolutions.webhookie.subscription.domain.Application> *(..))")
  fun returnsMonoApplication() {
  }

  @Around("annotatedVerifyApplicationReadAccess() && returnsMonoApplication()")
  fun checkReadAccess(pjp: ProceedingJoinPoint): Mono<Application> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<Application> = pjp.proceed() as Mono<Application>

    return securityService.verifyReadAccess {
      mono
        .doOnNext {
          if (log.isDebugEnabled) {
            log.debug("Verifying Application '{}' Read Access...", it.name)
          }
        }
    }
  }



}