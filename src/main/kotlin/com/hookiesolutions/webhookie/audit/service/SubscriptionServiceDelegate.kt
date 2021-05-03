package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.subscription.service.ApplicationService
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/3/21 10:26
 */
@Service
class SubscriptionServiceDelegate(
  private val applicationService: ApplicationService,
  private val subscriptionService: SubscriptionService
) {
  fun userApplications(): Flux<ApplicationDetails> {
    return applicationService.userApplications()
      .map { it.details() }
  }

  fun consumerTopics(): Mono<List<String>> {
    return subscriptionService.consumerSubscriptions(Pageable.unpaged(), null, null)
      .map { it.topic }
      .collectList()
  }
}
