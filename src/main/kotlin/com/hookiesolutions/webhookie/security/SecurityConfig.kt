/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.security

import com.hookiesolutions.webhookie.admin.web.admin.AdminAPIDocs.Companion.REQUEST_MAPPING_ADMIN
import com.hookiesolutions.webhookie.admin.web.admin.AdminConsumerGroupController.Companion.REQUEST_MAPPING_CONSUMER_GROUPS
import com.hookiesolutions.webhookie.admin.web.GroupAPIDocs.Companion.REQUEST_MAPPING_GROUP
import com.hookiesolutions.webhookie.admin.web.admin.AdminProviderGroupController.Companion.REQUEST_MAPPING_PROVIDER_GROUPS
import com.hookiesolutions.webhookie.audit.web.TrafficAPIDocs.Companion.REQUEST_MAPPING_TRAFFIC
import com.hookiesolutions.webhookie.audit.web.TrafficController.Companion.REQUEST_MAPPING_TRAFFIC_SPAN
import com.hookiesolutions.webhookie.audit.web.TrafficController.Companion.REQUEST_MAPPING_TRAFFIC_TRACE
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.common.web.CommonAPIDocs.Companion.REQUEST_MAPPING_USER_INFO
import com.hookiesolutions.webhookie.ingress.web.IngressAPIDocs.Companion.REQUEST_MAPPING_INGRESS
import com.hookiesolutions.webhookie.ingress.web.PublisherController.Companion.REQUEST_MAPPING_CONSUMER_EVENT
import com.hookiesolutions.webhookie.security.customizer.AllowAllPermissionEvaluator
import com.hookiesolutions.webhookie.security.customizer.DelegateAuthenticationEntryPoint
import com.hookiesolutions.webhookie.security.jwt.AudienceValidator
import com.hookiesolutions.webhookie.security.jwt.JwtAuthoritiesConverter
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_APPLICATIONS
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_PROVIDER
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.webhook.config.WebhookApiAPIDocs.Companion.REQUEST_MAPPING_WEBHOOK_APIS
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.Customizer
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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import javax.annotation.PostConstruct


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/5/20 03:47
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
  private val securityProperties: WebhookieSecurityProperties,
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
    methodSecurityExpressionHandler: DefaultMethodSecurityExpressionHandler,
  ): MethodSecurityExpressionHandler {
    methodSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator)
    return methodSecurityExpressionHandler
  }

  @Bean
  internal fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    http.cors(Customizer.withDefaults())
    return http {
      csrf { disable() }

      authorizeExchange {
        securityProperties.noAuth.pathMatchers
          .plus(HttpMethod.OPTIONS.name to arrayOf("/**"))
          .entries
          .forEach {
            authorize(pathMatchers(HttpMethod.valueOf(it.key), *it.value), permitAll)
          }

        authorize(pathMatchers(HttpMethod.GET,"$REQUEST_MAPPING_GROUP$REQUEST_MAPPING_PROVIDER_GROUPS"), authenticated)
        authorize(pathMatchers(HttpMethod.GET,"$REQUEST_MAPPING_GROUP$REQUEST_MAPPING_CONSUMER_GROUPS"), authenticated)
        authorize(pathMatchers("$REQUEST_MAPPING_ADMIN/**"), hasAuthority(ROLE_ADMIN))
        authorize(pathMatchers("$REQUEST_MAPPING_APPLICATIONS/**"), hasAuthority(ROLE_CONSUMER))
        authorize(pathMatchers("$REQUEST_MAPPING_INGRESS/$REQUEST_MAPPING_CONSUMER_EVENT/**"), hasAuthority(ROLE_PROVIDER))
        authorize(pathMatchers("$REQUEST_MAPPING_PROVIDER/**"), hasAnyAuthority(ROLE_PROVIDER, ROLE_ADMIN))
        authorize(pathMatchers("$REQUEST_MAPPING_SUBSCRIPTIONS/**"), authenticated)
        authorize(pathMatchers(HttpMethod.GET,"$REQUEST_MAPPING_USER_INFO/**"), authenticated)
        authorize(pathMatchers(HttpMethod.GET,"$REQUEST_MAPPING_TRAFFIC$REQUEST_MAPPING_TRAFFIC_SPAN/**"), hasAnyAuthority(ROLE_CONSUMER, ROLE_ADMIN))
        authorize(pathMatchers(HttpMethod.GET,"$REQUEST_MAPPING_TRAFFIC$REQUEST_MAPPING_TRAFFIC_TRACE/**"), hasAnyAuthority(ROLE_PROVIDER, ROLE_ADMIN))
        authorize(pathMatchers(HttpMethod.GET,"$REQUEST_MAPPING_WEBHOOK_APIS/**"), permitAll)

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

  @Bean
  fun corsConfigurationSource(securityProperties: WebhookieSecurityProperties): CorsConfigurationSource {
    val configuration = CorsConfiguration()
    configuration.allowedOrigins = securityProperties.allowedOrigins
    configuration.addAllowedMethod(CorsConfiguration.ALL)
    configuration.addAllowedHeader(CorsConfiguration.ALL)
    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", configuration)
    return source
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
