package com.hookiesolutions.webhookie.subscription.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO
import org.springframework.http.HttpMethod

data class CallbackDetails(
  @JsonProperty("id")
  val callbackId: String,
  val name: String,
  val httpMethod: HttpMethod,
  val url: String,
  val security: CallbackSecurity? = null,
) {
  fun requestTarget(): String {
    return "${httpMethod.name} $url"
  }

  fun dto(): CallbackDTO {
    return CallbackDTO(callbackId, name, httpMethod, url, security?.dto())
  }

  class Keys {
    companion object {
      const val KEY_CALLBACK_ID = "callbackId"
    }
  }

}