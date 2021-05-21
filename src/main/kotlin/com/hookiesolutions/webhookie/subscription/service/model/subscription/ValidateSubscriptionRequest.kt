package com.hookiesolutions.webhookie.subscription.service.model.subscription

import javax.validation.constraints.NotBlank

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/2/21 13:17
 */
data class ValidateSubscriptionRequest(
  @field:NotBlank
  val payload: String,
  val headers: Map<String, Any>,
)
