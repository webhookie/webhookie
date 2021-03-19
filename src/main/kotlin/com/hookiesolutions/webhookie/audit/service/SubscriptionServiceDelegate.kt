package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.subscription.service.ApplicationService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/3/21 10:26
 */
@Service
class SubscriptionServiceDelegate(
  private val service: ApplicationService
) {
  fun userApplications(): Flux<ApplicationDetails> {
    return service.userApplications()
      .map { it.details() }
  }
}
