package com.hookiesolutions.webhookie.subscription.enterprise.config

import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.MutableReactiveClientRegistrationRepository
import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.OAuth2AuthorizationHeaderProvider
import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.OAuth2CallbackAuthorizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository

@Configuration
class OAuth2ClientConfig {
  @Bean
  fun oauth2ClientRepository(): MutableReactiveClientRegistrationRepository {
    return MutableReactiveClientRegistrationRepository()
  }

  @Bean
  fun clientService(clientRegistrations: ReactiveClientRegistrationRepository): ReactiveOAuth2AuthorizedClientService {
    return InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations)
  }

  @Bean
  fun authorizedClientManager(
    repository: ReactiveClientRegistrationRepository,
    service: ReactiveOAuth2AuthorizedClientService
  ): ReactiveOAuth2AuthorizedClientManager {
    return AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(repository, service)
  }

  @Bean
  fun oAuth2CallbackAuthorizer(
    manager: ReactiveOAuth2AuthorizedClientManager,
    repository: MutableReactiveClientRegistrationRepository
  ): OAuth2CallbackAuthorizer {
    return OAuth2CallbackAuthorizer(manager, repository)
  }

  @Bean
  fun oAuth2AuthorizationHeaderProvider(
    oAuth2CallbackAuthorizer: OAuth2CallbackAuthorizer
  ): OAuth2AuthorizationHeaderProvider {
    return OAuth2AuthorizationHeaderProvider(oAuth2CallbackAuthorizer)
  }
}

