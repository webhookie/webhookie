package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO
import org.springframework.http.HttpMethod

data class CallbackDetails(
  val httpMethod: HttpMethod,
  val url: String,
  val security: CallbackSecurity? = null,
) {
  fun requestTarget(): String {
    return "${httpMethod.name} $url"
  }

  fun dto(): CallbackDTO {
    return CallbackDTO(url, httpMethod, security != null)
  }
}