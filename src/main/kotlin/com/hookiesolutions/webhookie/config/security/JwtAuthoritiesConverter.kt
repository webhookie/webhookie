package com.hookiesolutions.webhookie.config.security

import org.springframework.core.convert.converter.Converter
import org.springframework.integration.json.JsonPathUtils
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Component
class JwtAuthoritiesConverter(
  private val securityProperties: WebHookieSecurityProperties
) : Converter<Jwt, Mono<AbstractAuthenticationToken>> {
  override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken>? {
    return Mono.create { sink ->
      try {
        println(securityProperties.loginUrl)
        val aud: String = JsonPathUtils.evaluate(jwt.claims, securityProperties.iamAud.jsonPath)
        if (aud == securityProperties.iamAud.value) {
          sink.error(BadJwtException("INVALID_AUD"))
        } else {
          val list: List<String> = JsonPathUtils.evaluate(jwt.claims, securityProperties.roles.jwkJsonPath)
          val authorities = list.map { SimpleGrantedAuthority(it) }
          sink.success(JwtAuthenticationToken(jwt, authorities))
        }
      } catch (ex: Exception) {
        sink.error(ex)
      }
    }
  }
}