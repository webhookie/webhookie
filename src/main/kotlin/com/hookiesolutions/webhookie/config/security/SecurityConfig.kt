package com.hookiesolutions.webhookie.config.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/5/20 03:47
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(WebHookieSecurityProperties::class, NoAuth::class, RolesConfig::class, AudProperties::class)
class SecurityConfig(
  private val securityProperties: WebHookieSecurityProperties,
  private val jwtAuthoritiesConverter: JwtAuthoritiesConverter
) {
  @Bean
  internal fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    return http {
      csrf { disable() }

      authorizeExchange {
        securityProperties.noAuth.pathMatchers
          .plus(HttpMethod.OPTIONS.name to arrayOf("/**"))
          .entries
          .forEach {
            authorize(pathMatchers(HttpMethod.valueOf(it.key), *it.value), permitAll)
          }

        authorize()
      }

      oauth2ResourceServer {
        jwt {
          jwtAuthenticationConverter = jwtAuthoritiesConverter
        }
      }
    }
  }
}