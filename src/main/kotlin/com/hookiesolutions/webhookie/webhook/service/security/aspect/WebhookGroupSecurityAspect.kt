package com.hookiesolutions.webhookie.webhook.service.security.aspect

import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import com.hookiesolutions.webhookie.webhook.service.security.WebhookSecurityService
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
 * @since 20/1/21 01:51
 */

@Aspect
@Component
class WebhookGroupSecurityAspect(
  private val securityService: WebhookSecurityService,
  private val log: Logger
) {

  @Pointcut("@annotation(com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupConsumeAccess)")
  fun annotatedWithVerifyWebhookGroupConsumeAccess() {}

  @Pointcut("execution(reactor.core.publisher.Mono<com.hookiesolutions.webhookie.webhook.domain.WebhookGroup> *(..))")
  fun returnsMonoWebhookGroup() {}

  @Around("annotatedWithVerifyWebhookGroupConsumeAccess() && returnsMonoWebhookGroup()")
  fun checkAccess(pjp: ProceedingJoinPoint): Mono<WebhookGroup> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<WebhookGroup> = pjp.proceed() as Mono<WebhookGroup>

    return securityService.verifyConsumeAccess {
      mono
        .doOnNext {
          if(log.isDebugEnabled) {
            log.debug("Verifying WebhookGroup '{}' Consume Access...", it.name)
          }
        }
    }
  }
}