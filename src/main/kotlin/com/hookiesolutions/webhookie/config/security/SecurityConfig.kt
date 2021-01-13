package com.hookiesolutions.webhookie.config.security

import com.hookiesolutions.webhookie.config.security.jwt.AudienceValidator
import com.hookiesolutions.webhookie.config.security.jwt.JwtAuthoritiesConverter
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import javax.annotation.PostConstruct

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/5/20 03:47
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(WebHookieSecurityProperties::class)
class SecurityConfig(
  private val securityProperties: WebHookieSecurityProperties,
  private val audienceValidator: AudienceValidator,
  private val jwtDecoder: ReactiveJwtDecoder,
  private val resourceServerProperties: OAuth2ResourceServerProperties,
  private val jwtAuthoritiesConverter: JwtAuthoritiesConverter
) {

  //TODO: there must be a better way customizing DefaultMethodSecurityExpressionHandler for Webflux Security
  // Maybe look into https://www.youtube.com/watch?v=x0UE5THrSZM
  @Bean
  fun handler(
    permissionEvaluator: AllowAllPermissionEvaluator,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    methodSecurityExpressionHandler: DefaultMethodSecurityExpressionHandler
  ): MethodSecurityExpressionHandler {
    methodSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator)
    return methodSecurityExpressionHandler
  }

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

        authenticationEntryPoint = DelegateAuthenticationEntryPoint()
      }
    }
  }

  @PostConstruct
  fun customizeDecoder() {
    val validators = mutableListOf<OAuth2TokenValidator<Jwt>>(audienceValidator)
    if(resourceServerProperties.jwt.issuerUri != null) {
      val validator = JwtValidators.createDefaultWithIssuer(resourceServerProperties.jwt.issuerUri)
      validators.add(validator)
    }
    (jwtDecoder as NimbusReactiveJwtDecoder).setJwtValidator(DelegatingOAuth2TokenValidator(validators))
  }
}