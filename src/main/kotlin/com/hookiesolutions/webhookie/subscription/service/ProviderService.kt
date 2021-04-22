package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/4/21 14:05
 */
@Service
class ProviderService(
  private val subscriptionService: SubscriptionService
) {
  fun providerEntities(): Mono<Set<String>> {
    return mySubscriptions()
      .map { it.application.entity }
      .collectList()
      .map { it.toSet() }
  }

  fun providerEntityApplications(entity: String): Mono<Set<ApplicationDetails>> {
    return mySubscriptions()
      .filter { it.application.entity == entity }
      .map { it.application }
      .collectList()
      .map { list -> list.sortedWith { a1, a2 -> a1.name.compareTo(a2.name) }.toSet() }
  }

  fun applicationCallbacks(applicationId: String): Mono<Set<CallbackDetails>> {
    return mySubscriptions()
      .filter { it.application.applicationId == applicationId }
      .map { it.callback }
      .collectList()
      .map { list -> list.sortedWith { a1, a2 -> a1.name.compareTo(a2.name) }.toSet() }
  }

  private fun mySubscriptions(): Flux<Subscription> {
    return subscriptionService.providerSubscriptions(Pageable.unpaged())
  }
}
