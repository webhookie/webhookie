package com.hookiesolutions.webhookie.security

import com.hookiesolutions.webhookie.security.jwt.WebhookieJwtAuthenticationToken
import org.slf4j.Logger
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 28/1/21 15:55
 */
@Component
class AuthenticationSuccessHandler(
  private val log: Logger
): ServerAuthenticationSuccessHandler {
  override fun onAuthenticationSuccess(
    webFilterExchange: WebFilterExchange,
    authentication: Authentication,
  ): Mono<Void> {
    val auth = authentication as WebhookieJwtAuthenticationToken
    log.debug("authenticated user: '{}', '{}', '{}', '{}'", auth.entity, auth.email, auth.roles, auth.groups)

    return Mono.empty()
  }
}