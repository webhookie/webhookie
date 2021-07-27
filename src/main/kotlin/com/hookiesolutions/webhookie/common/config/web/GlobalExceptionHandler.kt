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

package com.hookiesolutions.webhookie.common.config.web

import com.hookiesolutions.webhookie.common.exception.*
import com.hookiesolutions.webhookie.common.service.ReactiveObjectMapper
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mapping.context.InvalidPersistentPropertyPath
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.FileNotFoundException

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 18:03
 */
@RestControllerAdvice
class GlobalExceptionHandler(
  private val om: ReactiveObjectMapper
) {
  @ExceptionHandler(WebExchangeBindException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handleValidationException(ex: WebExchangeBindException): Mono<MutableMap<String, String>> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
  }

  @ExceptionHandler(IllegalArgumentException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<MutableMap<String, String>> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
  }

  @ExceptionHandler(AccessDeniedException::class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  fun handleAccessDeniedException(ex: AccessDeniedException): Mono<Any> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
  }

  @ExceptionHandler(EntityNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun handleUserNotFoundException(ex: EntityNotFoundException): Mono<Any> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
  }

  @ExceptionHandler(DuplicateKeyException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun handleDuplicateKeyException(ex: DuplicateKeyException): Mono<Any> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
  }

  @ExceptionHandler(EntityExistsException::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun handleEntityExistsException(ex: EntityExistsException): Mono<Any> {
    return mutableMapOf(
      "message" to "Duplicate Key",
      "key" to ex.key.substringBefore(";").trim()
    )
      .toMono()
  }

  @ExceptionHandler(EmptyResultException::class)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun handleEmptyResultException(ex: EmptyResultException): Mono<Any> {
    return emptyList<String>().toMono()
  }

  @ExceptionHandler(FileNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun handleFileNotFoundException(ex: FileNotFoundException): Mono<Any> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
  }

  @ExceptionHandler(ValidationException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handleValidationException(ex: ValidationException): Mono<Any> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
  }

  @ExceptionHandler(WebClientResponseException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handleWebClientResponseException(ex: WebClientResponseException): Mono<Map<String, Any>> {
    return try {
      om.readMap(ex.responseBodyAsString)
    } catch (e: Exception) {
      mutableMapOf("message" to e.localizedMessage).toMono()
    }
  }

  @ExceptionHandler(ServerWebInputException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handleServerWebInputException(ex: ServerWebInputException): Mono<Any> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
  }

  @ExceptionHandler(BindException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handleBindException(ex: BindException): Mono<Any> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
  }

  @ExceptionHandler(InvalidPersistentPropertyPath::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handleInvalidPersistentPropertyPath(ex: InvalidPersistentPropertyPath): Mono<Any> {
    return mutableMapOf(
      "message" to ex.localizedMessage,
      "source" to ex.source,
      "unresolvableSegment" to ex.unresolvableSegment,
      "resolvedPath" to ex.resolvedPath,
    ).toMono()
  }

  @ExceptionHandler(RemoteServiceException::class)
  @ResponseStatus(HttpStatus.BAD_GATEWAY)
  fun handleRemoteServiceException(ex: RemoteServiceException): Mono<Any> {
    return mutableMapOf(
      "message" to ex.localizedMessage,
    ).toMono()
  }
}
