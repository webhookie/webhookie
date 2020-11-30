package com.hookiesolutions.webhookie.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 30/11/20 14:20
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {
  @Bean
  @Throws(Exception::class)
  internal fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    http
      .csrf().disable()
    return http.build()
  }
}