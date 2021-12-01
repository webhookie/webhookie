package com.hookiesolutions.webhookie.subscription.enterprise.oauth2

import com.hookiesolutions.webhookie.subscription.config.auth.AuthorizationHeaderProvider
import com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2.OAuth2SecurityScheme
import com.hookiesolutions.webhookie.subscription.service.model.CallbackValidationSampleRequest
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER
import reactor.core.publisher.Mono

class OAuth2AuthorizationHeaderProvider(
  private val oAuth2CallbackAuthorizer: OAuth2CallbackAuthorizer
): AuthorizationHeaderProvider() {
  override fun accepts(request: CallbackValidationSampleRequest): Boolean {
    return (request.securityScheme as? OAuth2SecurityScheme) != null
  }

  override fun customHeaders(request: CallbackValidationSampleRequest, headers: HttpHeaders): Mono<HttpHeaders> {
    return oAuth2CallbackAuthorizer.authorize(request.callback)
      .map { token ->
        headers[HttpHeaders.AUTHORIZATION] = listOf("${BEARER.value} $token")

        headers
      }
  }
}
