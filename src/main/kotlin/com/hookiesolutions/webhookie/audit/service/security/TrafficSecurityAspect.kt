package com.hookiesolutions.webhookie.audit.service.security

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.Trace
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/5/21 11:21
 */
@Aspect
@Component
class TrafficSecurityAspect(
  private val securityService: TrafficSecurityService
) {
  @Pointcut("@annotation(com.hookiesolutions.webhookie.audit.service.security.VerifySpanReadAccess)")
  fun annotatedVerifySpanReadAccess() {
  }

  @Pointcut("execution(reactor.core.publisher.Mono<com.hookiesolutions.webhookie.audit.domain.Span> *(..))")
  fun returnsMonoSpan() {
  }

  @Around("annotatedVerifySpanReadAccess() && returnsMonoSpan()")
  fun checkSpanReadAccess(pjp: ProceedingJoinPoint): Mono<Span> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<Span> = pjp.proceed() as Mono<Span>

    return securityService.verifySpanReadAccess(mono)
  }

  @Pointcut("execution(reactor.core.publisher.Mono<com.hookiesolutions.webhookie.audit.domain.Trace> *(..))")
  fun returnsMonoTrace() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.audit.service.security.VerifyTraceReadAccess)")
  fun annotatedVerifyTraceReadAccess() {
  }

  @Around("annotatedVerifyTraceReadAccess() && returnsMonoTrace()")
  fun checkTraceReadAccess(pjp: ProceedingJoinPoint): Mono<Trace> {
    @Suppress("UNCHECKED_CAST")
    val mono: Mono<Trace> = pjp.proceed() as Mono<Trace>

    return securityService.verifyTraceReadAccess(mono)
  }
}
