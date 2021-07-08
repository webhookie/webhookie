package com.hookiesolutions.webhookie.security.jwt

import com.hookiesolutions.webhookie.security.WebHookieSecurityProperties
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
  private val tokenGroupReader: TokenGroupReader,
  private val tokenAttributesExtractor: JwtTokenAttributesExtractor
) : Converter<Jwt, Mono<AbstractAuthenticationToken>> {
  override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken>? {
    val rolesMono = tokenAttributesExtractor
      .readList(jwt, securityProperties.roles.jwkJsonPath)
      .onErrorReturn(emptyList())
      .map { authoritiesMapper.map(it) }

    val groupsMono = tokenGroupReader.readGroups(jwt)

    val subMono: Mono<String> = tokenAttributesExtractor
      .read(jwt, "$.sub")

    val entityMono: Mono<String> = tokenAttributesExtractor
      .read(jwt, securityProperties.entity.jwkJsonPath)

    return Mono
      .zip(rolesMono, groupsMono, subMono, entityMono)
      .map {
        WebhookieJwtAuthenticationToken(jwt, it.t1, it.t2, it.t3, it.t4)
      }
  }
}
