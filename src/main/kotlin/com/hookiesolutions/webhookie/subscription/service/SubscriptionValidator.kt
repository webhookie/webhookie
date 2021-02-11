package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.service.model.CallbackValidationSampleRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.ValidateSubscriptionRequest
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/2/21 15:04
 */
@Service
class SubscriptionValidator(
  private val log: Logger,
  private val requestValidator: RequestValidator
) {
  fun validate(subscription: Subscription, request: ValidateSubscriptionRequest): Mono<Subscription> {
    val sampleRequest = CallbackValidationSampleRequest.Builder()
      .callbackDetails(subscription.callback)
      .payload(request.payload)
      .headers(request.headers)
      .build()

    log.info("Validating Subscription '{}' with callback: '{}'...", subscription.id, subscription.callback.requestTarget())

    return requestValidator.validateRequest(sampleRequest)
      .map { subscription }
  }
}