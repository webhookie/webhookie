package com.hookiesolutions.webhookie.webhook.service.security.aspect

import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import com.hookiesolutions.webhookie.webhook.service.security.WebhookSecurityService
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.Logger
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 20/1/21 01:51
 */

@Aspect
@Component
class WebhookGroupSecurityAspect(
  private val securityService: WebhookSecurityService,
  private val log: Logger
) {

  @Pointcut("@annotation(com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupReadAccess)")
  fun annotatedVerifyWebhookGroupReadAccess() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupWriteAccess)")
  fun annotatedVerifyWebhookGroupWriteAccess() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupCanBeDeleted)")
  fun annotatedVerifyWebhookGroupCanBeDeleted() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupCanBeUpdated)")
  fun annotatedVerifyWebhookGroupCanBeUpdated() {
  }

  @Pointcut("execution(reactor.core.publisher.Mono<com.hookiesolutions.webhookie.webhook.domain.WebhookGroup> *(..))")
  fun returnsMonoWebhookGroup() {
  }

  @Around("annotatedVerifyWebhookGroupReadAccess() && returnsMonoWebhookGroup()")
  fun checkReadAccess(pjp: ProceedingJoinPoint): Mono<WebhookGroup> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<WebhookGroup> = pjp.proceed() as Mono<WebhookGroup>

    return securityService.verifyReadAccess {
      mono
        .doOnNext {
          if (log.isDebugEnabled) {
            log.debug("Verifying WebhookGroup '{}' Consume Access...", it.title)
          }
        }
    }
  }

  @Around("annotatedVerifyWebhookGroupWriteAccess() && returnsMonoWebhookGroup()")
  fun checkWriteAccess(pjp: ProceedingJoinPoint): Any {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<WebhookGroup> = pjp.proceed() as Mono<WebhookGroup>

    return securityService.verifyWriteAccess {
      mono
        .doOnNext {
          if (log.isDebugEnabled) {
            log.debug("Verifying WebhookGroup '{}' Write Access...", it.title)
          }
        }
    }
  }

  @Before("annotatedVerifyWebhookGroupCanBeDeleted() && args(deletableEntity)")
  fun checkDeleteAccess(jp: JoinPoint, deletableEntity: DeletableEntity<WebhookGroup>) {
    if(!deletableEntity.deletable) {
      throw AccessDeniedException("Entity is not deletable!")
    }
  }

  @Before("annotatedVerifyWebhookGroupCanBeUpdated() && args(updatableEntity)")
  fun checkUpdateAccess(jp: JoinPoint, updatableEntity: UpdatableEntity<WebhookGroup>) {
    if(!updatableEntity.updatable) {
      throw AccessDeniedException("Entity is not updatable!")
    }
  }
}
