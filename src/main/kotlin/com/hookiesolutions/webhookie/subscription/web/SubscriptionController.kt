package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.config.web.OpenAPIConfig
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import com.hookiesolutions.webhookie.subscription.web.CompanyController.Companion.REQUEST_MAPPING_COMPANY
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 18:20
 */
@RestController
@SecurityRequirement(name = OpenAPIConfig.BASIC_SCHEME)
@Validated
class SubscriptionController(
  private val log: Logger,
  private val subscriptionService: SubscriptionService,
) {
  @PatchMapping("$REQUEST_MAPPING_COMPANY$REQUEST_MAPPING_SUBSCRIPTION/{id}/unblock", produces = [MediaType.TEXT_PLAIN_VALUE])
  fun unblockSubscription(@PathVariable id: String): Mono<String> {
    log.info("Unblocking subscription: '{}'", id)
    return subscriptionService.unblockSubscriptionBy(id)
      .map { it.id!! }
  }

  companion object {
    const val REQUEST_MAPPING_SUBSCRIPTION = "/subscription"
  }
}