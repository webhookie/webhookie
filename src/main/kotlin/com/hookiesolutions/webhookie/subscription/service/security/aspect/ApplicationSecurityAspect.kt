package com.hookiesolutions.webhookie.subscription.service.security.aspect

import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.Callback
import com.hookiesolutions.webhookie.subscription.service.security.ApplicationSecurityService
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationReadAccessById
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationWriteAccessById
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.springframework.stereotype.Component
import reactor.core.CorePublisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.Method

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 29/1/21 17:40
 */
@Aspect
@Component
class ApplicationSecurityAspect(
  private val securityService: ApplicationSecurityService,
  private val applicationRepository: ApplicationRepository,
  private val log: Logger,
) {
  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationReadAccess)")
  fun annotatedVerifyApplicationReadAccess() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationReadAccessById)")
  fun annotatedVerifyApplicationReadAccessById() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationWriteAccessById)")
  fun annotatedVerifyApplicationWriteAccessById() {
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

    return securityService
      .verifyReadAccess {
        mono
          .doOnNext {
            if (log.isDebugEnabled) {
              log.debug("Verifying Application '{}' Read Access...", it.name)
            }
          }
      }
  }

  @Around("annotatedVerifyApplicationReadAccessById()")
  fun checkReadAccessById(pjp: ProceedingJoinPoint): Any {
    val method: Method = extractMethod(pjp)

    val ann = method.getAnnotation(VerifyApplicationReadAccessById::class.java)

    val id = pjp.args[ann.idPosition] as String
    val applicationMono = applicationRepository.findByIdVerifyingReadAccess(id)
    return applicationData(pjp, applicationMono)
  }

  @Around("annotatedVerifyApplicationWriteAccessById()")
  fun checkWriteAccessById(pjp: ProceedingJoinPoint): Any {
    val method: Method = extractMethod(pjp)

    val ann = method.getAnnotation(VerifyApplicationWriteAccessById::class.java)

    val id = pjp.args[ann.idPosition] as String
    val applicationMono = applicationRepository.findByIdVerifyingWriteAccess(id)
    return applicationData(pjp, applicationMono)
  }

  private fun extractMethod(pjp: ProceedingJoinPoint): Method {
    return (pjp.signature as MethodSignature).method
  }

  private fun applicationData(pjp: ProceedingJoinPoint, applicationMono: Mono<Application>): CorePublisher<Callback> {
    val returnType: Class<*> = extractMethod(pjp).returnType
    return if (Mono::class.java.isAssignableFrom(returnType)) {
      @Suppress("UNCHECKED_CAST")
      val mono = pjp.proceed() as Mono<Callback>
      applicationMono.flatMap { mono }
    } else {
      @Suppress("UNCHECKED_CAST")
      val flux = pjp.proceed() as Flux<Callback>
      applicationMono.flatMapMany { flux }
    }
  }

  @Around("annotatedVerifyApplicationWriteAccess() && returnsMonoApplication()")
  fun checkWriteAccess(pjp: ProceedingJoinPoint): Mono<Application> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<Application> = pjp.proceed() as Mono<Application>

    return securityService
      .verifyWriteAccess {
        mono
          .doOnNext {
            if (log.isDebugEnabled) {
              log.debug("Verifying Application '{}' Write Access...", it.name)
            }
          }
      }
  }
}