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
