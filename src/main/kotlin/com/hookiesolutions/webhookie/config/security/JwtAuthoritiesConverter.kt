package com.hookiesolutions.webhookie.config.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
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
  private val authoritiesMapper: AuthoritiesMapper,
  private val tokenAttributesExtractor: JwtTokenAttributesExtractor
) : Converter<Jwt, Mono<AbstractAuthenticationToken>> {
  override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken>? {
    return tokenAttributesExtractor.read(jwt)
      .map { authoritiesMapper.map(it) }
      .map { JwtAuthenticationToken(jwt, it) }
  }
}