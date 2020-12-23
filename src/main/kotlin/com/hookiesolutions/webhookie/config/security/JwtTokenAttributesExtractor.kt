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
  fun read(jwt: Jwt): Mono<JwtAttributes> {
    return Mono.create { sink ->
      try {
        val aud: String = JsonPathUtils.evaluate(jwt.claims, securityProperties.iamAud.jsonPath)
        if (aud != securityProperties.iamAud.value) {
          log.error("Provided aud: '{}' in the token doesn't match with the instance aud: '{}'", aud, securityProperties.iamAud.value)
          sink.error(BadJwtException("Invalid aud: $aud"))
        } else {
          val roles: List<String> = JsonPathUtils.evaluate(jwt.claims, securityProperties.roles.jwkJsonPath)
          sink.success(JwtAttributes(aud, roles))
        }
      } catch (ex: Exception) {
        log.error("Unable to extract jwt values! cause: '{}'", ex.localizedMessage)
        sink.error(BadJwtException(ex.localizedMessage, ex))
      }
    }
  }
}