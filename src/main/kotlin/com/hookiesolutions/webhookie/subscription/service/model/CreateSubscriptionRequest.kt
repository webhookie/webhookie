package com.hookiesolutions.webhookie.subscription.service.model

import com.hookiesolutions.webhookie.common.validation.ObjectId
import javax.validation.constraints.NotBlank

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 23:22
 */
data class CreateSubscriptionRequest(
  @field:NotBlank
  val topic: String,
  @field:NotBlank
  @field:ObjectId
  val callbackId: String
)