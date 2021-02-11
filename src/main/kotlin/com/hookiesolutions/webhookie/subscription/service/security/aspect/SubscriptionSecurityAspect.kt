package com.hookiesolutions.webhookie.subscription.service.security.aspect

import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.service.security.ApplicationSecurityService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 29/1/21 17:40
 */
@Aspect
@Component
class SubscriptionSecurityAspect(
  private val securityService: ApplicationSecurityService
) {
  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifySubscriptionReadAccess)")
  fun annotatedVerifySubscriptionReadAccess() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifySubscriptionWriteAccess)")
  fun annotatedVerifySubscriptionWriteAccess() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifySubscriptionProviderAccess)")
  fun annotatedVerifySubscriptionProviderAccess() {
  }

  @Pointcut("execution(reactor.core.publisher.Mono<com.hookiesolutions.webhookie.subscription.domain.Subscription> *(..))")
  fun returnsMonoSubscription() {
  }

  @Around("annotatedVerifySubscriptionReadAccess() && returnsMonoSubscription()")
  fun checkReadAccess(pjp: ProceedingJoinPoint): Mono<Subscription> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<Subscription> = pjp.proceed() as Mono<Subscription>

    return securityService.verifySubscriptionReadAccess(mono)
  }

  @Around("annotatedVerifySubscriptionWriteAccess() && returnsMonoSubscription()")
  fun checkWriteAccess(pjp: ProceedingJoinPoint): Mono<Subscription> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<Subscription> = pjp.proceed() as Mono<Subscription>

    return securityService.verifySubscriptionWriteAccess(mono)
  }

  @Around("annotatedVerifySubscriptionProviderAccess() && returnsMonoSubscription()")
  fun checkProviderAccess(pjp: ProceedingJoinPoint): Mono<Subscription> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<Subscription> = pjp.proceed() as Mono<Subscription>

    return securityService.verifySubscriptionProviderAccess(mono)
  }
}