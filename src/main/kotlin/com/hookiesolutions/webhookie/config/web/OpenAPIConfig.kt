package com.hookiesolutions.webhookie.config.web

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:33
 */
@Configuration
/*
@OpenAPIDefinition(
    info = Info(
        title = "webhookie API",
        version = "1.0.0"
    )
)
*/
//@SecurityScheme(
//  name = OpenAPIConfig.OAUTH2_SCHEME,
//  type = SecuritySchemeType.OAUTH2,
//  scheme = "oauth2",
////  openIdConnectUrl = "https://webhookie.au.auth0.com/.well-known/openid-configuration",
//  flows = OAuthFlows(
//    authorizationCode = OAuthFlow(
//      authorizationUrl = "https://webhookie.au.auth0.com/authorize",
//      tokenUrl = "https://webhookie.au.auth0.com/oauth/token"
//    )
//  ),
//
//  extensions = [
//    Extension(name = "x-tokenName", properties = [
//      ExtensionProperty(name = "id_token", value = "id_token")
//    ])
//  ]
//)
class OpenAPIConfig {
  companion object {
    const val OAUTH2_SCHEME = "oauth2Scheme"
  }
}