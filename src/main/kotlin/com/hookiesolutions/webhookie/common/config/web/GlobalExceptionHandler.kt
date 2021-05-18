package com.hookiesolutions.webhookie.common.config.web

import com.hookiesolutions.webhookie.common.exception.EmptyResultException
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.exception.ValidationException
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
class GlobalExceptionHandler {
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
  fun handleWebClientResponseException(ex: WebClientResponseException): Mono<Any> {
    return mutableMapOf("message" to ex.localizedMessage).toMono()
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
}
