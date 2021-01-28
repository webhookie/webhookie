package com.hookiesolutions.webhookie.security

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.springframework.boot.web.reactive.filter.OrderedWebFilter
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 28/1/21 15:54
 */
@Component
class AuthenticatedWebFilter(
  private val securityHandler: SecurityHandler,
  private val successHandler: ServerAuthenticationSuccessHandler
): OrderedWebFilter {
  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    return securityHandler
      .token()
      .doOnNext {
        successHandler.onAuthenticationSuccess(WebFilterExchange(exchange, chain), it)
      }
      .flatMap { chain.filter(exchange) }
  }

  override fun getOrder(): Int {
    return SecurityWebFiltersOrder.AUTHENTICATION.order
  }
}