/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

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
