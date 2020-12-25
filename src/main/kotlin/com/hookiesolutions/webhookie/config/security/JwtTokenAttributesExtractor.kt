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
  private val securityProperties: WebHookieSecurityProperties
) {
  fun read(jwt: Jwt): Mono<List<String>> {
    return Mono.create { sink ->
      try {
        val roles: List<String> = JsonPathUtils.evaluate(jwt.claims, securityProperties.roles.jwkJsonPath)
        sink.success(roles)
      } catch (ex: Exception) {
        log.error("Unable to extract jwt values! cause: '{}'", ex.localizedMessage)
        sink.error(BadJwtException(ex.localizedMessage, ex))
      }
    }
  }
}