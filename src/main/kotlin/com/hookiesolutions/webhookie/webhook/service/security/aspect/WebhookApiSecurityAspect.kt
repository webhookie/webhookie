package com.hookiesolutions.webhookie.webhook.service.security.aspect

import com.hookiesolutions.webhookie.webhook.domain.WebhookApi
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
class WebhookApiSecurityAspect(
  private val securityService: WebhookSecurityService,
  private val log: Logger
) {
  @Pointcut("@annotation(com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookApiReadAccess)")
  fun annotatedVerifyWebhookApiReadAccess() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookApiWriteAccess)")
  fun annotatedVerifyWebhookApiWriteAccess() {
  }

  @Pointcut("execution(reactor.core.publisher.Mono<com.hookiesolutions.webhookie.webhook.domain.WebhookApi> *(..))")
  fun returnsMonoWebhookApi() {
  }

  @Around("annotatedVerifyWebhookApiReadAccess() && returnsMonoWebhookApi()")
  fun checkReadAccess(pjp: ProceedingJoinPoint): Mono<WebhookApi> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<WebhookApi> = pjp.proceed() as Mono<WebhookApi>

    return securityService.verifyReadAccess {
      mono
        .doOnNext {
          if (log.isDebugEnabled) {
            log.debug("Verifying WebhookApi '{}' Consume Access...", it.title)
          }
        }
    }
  }

  @Around("annotatedVerifyWebhookApiWriteAccess() && returnsMonoWebhookApi()")
  fun checkWriteAccess(pjp: ProceedingJoinPoint): Any {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<WebhookApi> = pjp.proceed() as Mono<WebhookApi>

    return securityService.verifyWriteAccess {
      mono
        .doOnNext {
          if (log.isDebugEnabled) {
            log.debug("Verifying WebhookApi '{}' Write Access...", it.title)
          }
        }
    }
  }
}
