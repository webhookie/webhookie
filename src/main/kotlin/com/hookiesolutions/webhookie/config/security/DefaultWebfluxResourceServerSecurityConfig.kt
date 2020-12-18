package com.hookiesolutions.webhookie.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/5/20 03:47
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class DefaultWebfluxResourceServerSecurityConfig(
  private val securityProperties: WebHookieSecurityProperties,
) {
  @Bean
  fun jwtAuthoritiesAwareAuthConverter() = JwtAuthoritiesAuthenticationConverter(JwtAuthenticationConverter())

  @Bean
  internal fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    http
      .csrf().disable()
      .authorizeExchange {
        securityProperties.noAuth.pathMatchers
          .entries
          .forEach { entry ->
            it.pathMatchers(HttpMethod.valueOf(entry.key), *entry.value).permitAll()
          }

        it
          .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
          .anyExchange().authenticated()
      }
      .oauth2ResourceServer()
      .jwt()
      .jwtAuthenticationConverter(jwtAuthoritiesAwareAuthConverter())
    return http.build()
  }
}