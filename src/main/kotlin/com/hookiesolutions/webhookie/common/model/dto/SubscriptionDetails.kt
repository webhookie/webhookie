package com.hookiesolutions.webhookie.common.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/3/21 11:04
 */
data class SubscriptionDetails(
  @JsonProperty("id")
  val subscriptionId: String,
  val application: ApplicationDetails,
  val topic: String,
  val callback: CallbackDTO,
) {
  class Keys {
    companion object {
      const val KEY_APPLICATION = "application"
      const val KEY_CALLBACK = "callback"
      const val KEY_TOPIC = "topic"
    }
  }

  companion object {
    fun from(subscriptionDTO: SubscriptionDTO): SubscriptionDetails {
      return SubscriptionDetails(
        subscriptionDTO.subscriptionId,
        subscriptionDTO.application,
        subscriptionDTO.topic,
        subscriptionDTO.callback
      )
    }
  }
}
