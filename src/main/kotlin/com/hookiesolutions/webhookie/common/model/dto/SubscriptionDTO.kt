package com.hookiesolutions.webhookie.common.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 01:25
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubscriptionDTO(
  @JsonProperty("id")
  val subscriptionId: String,
  val application: ApplicationDetails,
  val topic: String,
  val callback: CallbackDTO,
  val statusUpdate: StatusUpdate
) {
  val isBlocked: Boolean
    get() = statusUpdate.status == SubscriptionStatus.BLOCKED

  class Keys {
    companion object {
      const val KEY_SUBSCRIPTION_ID = "subscriptionId"
    }
  }
}
