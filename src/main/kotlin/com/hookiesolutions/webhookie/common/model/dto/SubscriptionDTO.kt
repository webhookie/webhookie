package com.hookiesolutions.webhookie.common.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.hookiesolutions.webhookie.subscription.domain.ApplicationDetails

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 01:25
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubscriptionDTO(
  val id: String,
  val application: ApplicationDetails,
  val topic: String,
  val callback: CallbackDTO,
  val blockedDetails: BlockedDetailsDTO? = null
) {
  val isBlocked: Boolean
    get() = blockedDetails != null
}
