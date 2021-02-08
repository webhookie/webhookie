package com.hookiesolutions.webhookie.subscription.domain

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 5/2/21 15:21
 */
data class ApplicationDetails(
  @JsonProperty("id")
  val applicationId: String,
  val name: String,
  val entity: String
) {
  class Keys {
    companion object {
      const val KEY_APPLICATION_ID = "applicationId"
    }
  }
}
