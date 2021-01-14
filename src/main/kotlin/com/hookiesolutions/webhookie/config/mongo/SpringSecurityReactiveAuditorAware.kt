package com.hookiesolutions.webhookie.config.mongo

import com.hookiesolutions.webhookie.config.security.jwt.WebhookieJwtAuthenticationToken
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 14/1/21 11:54
 */
@Component
class SpringSecurityReactiveAuditorAware: ReactiveAuditorAware<String> {
  override fun getCurrentAuditor(): Mono<String> {
    return ReactiveSecurityContextHolder.getContext()
      .map(SecurityContext::getAuthentication)
      .filter(Authentication::isAuthenticated)
      .cast(WebhookieJwtAuthenticationToken::class.java)
      .map {
        it.email
      }
  }
}