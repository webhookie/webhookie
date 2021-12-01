package com.hookiesolutions.webhookie.subscription.enterprise.oauth2

import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

class MutableReactiveClientRegistrationRepository : ReactiveClientRegistrationRepository {
  private val clientIdToClientRegistration: MutableMap<String, ClientRegistration> = ConcurrentHashMap()

  override fun findByRegistrationId(registrationId: String): Mono<ClientRegistration> {
    return Mono.justOrEmpty(clientIdToClientRegistration[registrationId])
  }

  fun registerClient(registration: ClientRegistration) {
    this.clientIdToClientRegistration[registration.registrationId] = registration
  }

}
