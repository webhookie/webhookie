package com.hookiesolutions.webhookie.subscription.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails.Keys.Companion.KEY_CALLBACK_ID
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails.Keys.Companion.KEY_METHOD
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails.Keys.Companion.KEY_NAME
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails.Keys.Companion.KEY_URL
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

  fun json(): String {
    return """
      {
            $KEY_CALLBACK_ID: '$callbackId',
            $KEY_NAME: '$name',
            $KEY_METHOD: '$httpMethod',
            $KEY_URL: '$url',
            
      }
    """.trimIndent()
  }

  class Keys {
    companion object {
      const val KEY_CALLBACK_ID = "callbackId"
      const val KEY_NAME = "name"
      const val KEY_METHOD = "httpMethod"
      const val KEY_URL = "url"
    }
  }

}