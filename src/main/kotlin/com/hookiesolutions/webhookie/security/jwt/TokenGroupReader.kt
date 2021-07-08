package com.hookiesolutions.webhookie.security.jwt

import com.hookiesolutions.webhookie.common.Constants.Companion.DEFAULT_CONSUMER_GROUP
import com.hookiesolutions.webhookie.consumer.config.ConsumerProperties
import com.hookiesolutions.webhookie.security.WebHookieSecurityProperties
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 01:16
 */
@Component
class TokenGroupReader(
  private val securityProperties: WebHookieSecurityProperties,
  private val tokenAttributesExtractor: JwtTokenAttributesExtractor,
  private val consumerProperties: ConsumerProperties
) {
  fun readGroups(jwt: Jwt): Mono<List<String>> {
    return tokenAttributesExtractor
      .readList(jwt, securityProperties.groups.jwkJsonPath)
      .onErrorReturn(emptyList())
      .map { enrichGroups(it) }
  }

  private fun enrichGroups(tokenGroups: List<String>): List<String> {
    return if(consumerProperties.addDefaultGroup) {
      tokenGroups.plus(DEFAULT_CONSUMER_GROUP)
    } else {
      tokenGroups
    }
  }
}
