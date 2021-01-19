package com.hookiesolutions.webhookie.common.config.web

import com.hookiesolutions.webhookie.security.WebHookieSecurityProperties
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:33
 */
@Configuration
class OpenAPIConfig {
  @Bean
  fun openAPI(
    openApiInfo: Info,
    openApiSecurityScheme: SecurityScheme
  ): OpenAPI {
    return OpenAPI()
      .info(openApiInfo)
      .components(
        Components()
          .addSecuritySchemes(OAUTH2_SCHEME, openApiSecurityScheme)
      )
  }

  @Bean
  fun openApiInfo(): Info {
    return Info()
      .title("webhookie API")
      .version("1.0.0")
  }

  @Bean
  fun openApiSecurityScheme(
    oauthFlow: OAuthFlow,
    securityProperties: WebHookieSecurityProperties
  ) : SecurityScheme {
    return SecurityScheme()
      .scheme(OAUTH2_SCHEME)
      .name(OAUTH2_SCHEME)
      .type(SecurityScheme.Type.OAUTH2)
      .flows(
        OAuthFlows()
          .password(oauthFlow)
      )
      .extensions(mapOf("x-tokenName" to securityProperties.oauth2.tokenName))
  }

  @Bean
  fun oauthFlow(
    resourceServerProperties: OAuth2ResourceServerProperties,
    securityProperties: WebHookieSecurityProperties
  ): OAuthFlow {
    val issuerUri = resourceServerProperties.jwt.issuerUri
    val authorizationUrl = "$issuerUri${securityProperties.oauth2.authorizationUri}"
    val tokenUrl = "$issuerUri${securityProperties.oauth2.tokenUri}"
    return OAuthFlow()
      .authorizationUrl(authorizationUrl)
      .tokenUrl(tokenUrl)
  }

  companion object {
    const val OAUTH2_SCHEME = "oauth2Scheme"
  }
}