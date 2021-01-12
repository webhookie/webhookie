package com.hookiesolutions.webhookie.config.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Component
class JwtAuthoritiesConverter(
  private val securityProperties: WebHookieSecurityProperties,
  private val authoritiesMapper: AuthoritiesMapper,
  private val tokenAttributesExtractor: JwtTokenAttributesExtractor
) : Converter<Jwt, Mono<AbstractAuthenticationToken>> {
  override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken>? {
    val rolesMono = tokenAttributesExtractor.readList(jwt, securityProperties.roles.jwkJsonPath)
    val groupsMono = tokenAttributesExtractor.readList(jwt, securityProperties.groups.jwkJsonPath)
    return rolesMono
      .map { authoritiesMapper.map(it) }
      .zipWith(groupsMono)
      .map { WebhookieJwtAuthenticationToken(jwt, it.t1, it.t2) }
  }
}