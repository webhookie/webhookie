package com.hookiesolutions.webhookie.subscription.service.security.aspect

import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.service.security.ApplicationSecurityService
import com.hookiesolutions.webhookie.subscription.service.security.annotation.ApplicationAccessType
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationAccessById
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
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
  private val applicationRepository: ApplicationRepository
) {
  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationReadAccess)")
  fun annotatedVerifyApplicationReadAccess() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationWriteAccess)")
  fun annotatedVerifyApplicationWriteAccess() {
  }

  @Pointcut("execution(reactor.core.publisher.Mono<com.hookiesolutions.webhookie.subscription.domain.Application> *(..))")
  fun returnsMonoApplication() {
  }

  @Around("annotatedVerifyApplicationReadAccess() && returnsMonoApplication()")
  fun checkReadAccess(pjp: ProceedingJoinPoint): Mono<Application> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<Application> = pjp.proceed() as Mono<Application>

    return securityService.verifyReadAccess(mono)
  }

  @Around("annotatedVerifyApplicationWriteAccess() && returnsMonoApplication()")
  fun checkWriteAccess(pjp: ProceedingJoinPoint): Mono<Application> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<Application> = pjp.proceed() as Mono<Application>

    return securityService.verifyWriteAccess(mono)
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationAccessById)")
  fun annotatedVerifyApplicationAccessById() {
  }

  @Around("annotatedVerifyApplicationAccessById()")
  fun checkAccessById(pjp: ProceedingJoinPoint): Any {
    val method = (pjp.signature as MethodSignature).method
    val returnType: Class<*> = method.returnType
    val annotation = method.getAnnotation(VerifyApplicationAccessById::class.java)
    val id = pjp.args[annotation.idPosition] as String
    val applicationMono = Mono.defer {
      return@defer if(annotation.access == ApplicationAccessType.READ) {
        applicationRepository.findByIdVerifyingReadAccess(id)
      } else {
        applicationRepository.findByIdVerifyingWriteAccess(id)
      }
    }

    return if (Mono::class.java.isAssignableFrom(returnType)) {
      applicationMono
        .flatMap {
          @Suppress("UNCHECKED_CAST")
          pjp.proceed() as Mono<*>
        }
    } else {
      applicationMono
        .flatMapMany {
          @Suppress("UNCHECKED_CAST")
          pjp.proceed() as Flux<*>
        }
    }
  }
}