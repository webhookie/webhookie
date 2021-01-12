package com.hookiesolutions.webhookie.config.security

import org.slf4j.Logger
import org.springframework.integration.json.JsonPathUtils
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Component
class JwtTokenAttributesExtractor(
  private val log: Logger,
) {
  fun readList(jwt: Jwt, claimJsonPath: String): Mono<List<String>> {
    return read(jwt, claimJsonPath)
  }

  fun <T> read(jwt: Jwt, claimJsonPath: String): Mono<T> {
    return Mono.create { sink ->
      try {
        val value: T = JsonPathUtils.evaluate(jwt.claims, claimJsonPath)
        sink.success(value)
      } catch (ex: Exception) {
        val message = "Unable to extract jwt values for path: '$claimJsonPath'!"
        log.error(message, ex)
        sink.error(BadJwtException(message, ex))
      }
    }
  }
}