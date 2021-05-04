package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
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
  @PreAuthorize("hasAnyAuthority('$ROLE_PROVIDER', '$ROLE_ADMIN')")
  fun providerEntities(): Mono<Set<String>> {
    return mySubscriptions()
      .map { list ->
        list
          .map { it.application.entity }
          .toSet()
      }
  }

  @PreAuthorize("hasAnyAuthority('$ROLE_PROVIDER', '$ROLE_ADMIN')")
  fun providerEntityApplications(entity: String): Mono<Set<ApplicationDetails>> {
    return mySubscriptions()
      .map { list ->
        list
          .filter { it.application.entity == entity }
          .map { it.application }
          .sortedWith { a1, a2 -> a1.name.compareTo(a2.name) }
          .toSet()
      }
  }

  @PreAuthorize("hasAnyAuthority('$ROLE_PROVIDER', '$ROLE_ADMIN')")
  fun applicationCallbacks(applicationId: String): Mono<Set<CallbackDetails>> {
    return mySubscriptions()
      .map { list ->
        list
          .filter { it.application.applicationId == applicationId }
          .map { it.callback }
          .sortedWith { a1, a2 -> a1.name.compareTo(a2.name) }
          .toSet()
      }
  }

  private fun mySubscriptions(): Mono<List<Subscription>> {
    return subscriptionService.providerSubscriptions(null, Pageable.unpaged())
      .collectList()
  }
}
