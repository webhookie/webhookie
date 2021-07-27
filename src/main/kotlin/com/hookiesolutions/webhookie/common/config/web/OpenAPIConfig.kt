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
          .authorizationCode(oauthFlow)
      )
      .extensions(mapOf("x-tokenName" to securityProperties.oauth2.tokenName))
  }

  @Bean
  fun oauthFlow(
    resourceServerProperties: OAuth2ResourceServerProperties,
    securityProperties: WebHookieSecurityProperties
  ): OAuthFlow {
    val issuerUri = resourceServerProperties.jwt.issuerUri
    val authorizationUrl = "$issuerUri${securityProperties.oauth2.authorizationUri}?audience=${securityProperties.audience}"
    val tokenUrl = "$issuerUri${securityProperties.oauth2.tokenUri}"
    return OAuthFlow()
      .authorizationUrl(authorizationUrl)
      .tokenUrl(tokenUrl)
  }

  companion object {
    const val OAUTH2_SCHEME = "oauth2Scheme"
  }
}
