package com.hookiesolutions.webhookie.security.customizer

import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class DelegateAuthenticationEntryPoint : ServerAuthenticationEntryPoint {
  private val bearerEntryPoint = BearerTokenServerAuthenticationEntryPoint()
  @Suppress("unused")
  private val redirectServerAuthenticationEntryPoint = RedirectServerAuthenticationEntryPoint("http://www.yahoo.com")

  override fun commence(exchange: ServerWebExchange, ex: AuthenticationException): Mono<Void> {
    return bearerEntryPoint.commence(exchange, ex)
/*
    return if(ex is AuthenticationCredentialsNotFoundException) {
      redirectServerAuthenticationEntryPoint.commence(exchange, ex)
    } else {
      bearerEntryPoint.commence(exchange, ex)
    }
*/
  }
}