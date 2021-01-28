package com.hookiesolutions.webhookie.security.service

import com.hookiesolutions.webhookie.security.jwt.WebhookieJwtAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 19:36
 */
@Component
class SecurityHandler {
  fun token(): Mono<WebhookieJwtAuthenticationToken> {
    return ReactiveSecurityContextHolder.getContext()
      .map(SecurityContext::getAuthentication)
      .filter(Authentication::isAuthenticated)
      .cast(WebhookieJwtAuthenticationToken::class.java)
  }

  fun groups(): Mono<List<String>> {
    return token()
      .map { it.groups }
  }

  fun entity(): Mono<String> {
    return token()
      .map { it.entity }
  }
}