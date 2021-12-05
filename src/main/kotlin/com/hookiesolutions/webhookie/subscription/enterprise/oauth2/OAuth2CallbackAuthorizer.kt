package com.hookiesolutions.webhookie.subscription.enterprise.oauth2

import com.hookiesolutions.webhookie.common.exception.RemoteServiceException
import com.hookiesolutions.webhookie.subscription.domain.callback.CallbackDetails
import com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2.ClientCredentialsGrantType
import com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2.OAuth2SecurityScheme
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import reactor.core.publisher.Mono

class OAuth2CallbackAuthorizer(
  private val manager: ReactiveOAuth2AuthorizedClientManager,
  private val subscriptionService: SubscriptionService,
  private val repository: MutableReactiveClientRegistrationRepository
) {
  fun authorize(subscriptionId: String): Mono<String> {
    return subscriptionService.subscriptionById(subscriptionId)
      .flatMap { authorize(it.callback) }
  }

  fun authorize(callback: CallbackDetails): Mono<String> {
    val scheme = callback.securityScheme as OAuth2SecurityScheme
    val details = scheme.details as ClientCredentialsGrantType
    val registration: ClientRegistration = ClientRegistration
      .withRegistrationId(callback.requestTarget() + " -" + details.clientId)
      .tokenUri(details.tokenEndpoint)
      .clientId(details.clientId)
      .clientSecret(details.secret)
      .scope(details.scopes)
      .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
      .build()

    repository.registerClient(registration)

    val authorizeRequest = OAuth2AuthorizeRequest
      .withClientRegistrationId(registration.registrationId)
      .principal("webhookie")
      .build()

    return manager.authorize(authorizeRequest)
      .map {
        it.accessToken.tokenValue
      }
      .onErrorMap { RemoteServiceException(it.localizedMessage) }
  }
}
