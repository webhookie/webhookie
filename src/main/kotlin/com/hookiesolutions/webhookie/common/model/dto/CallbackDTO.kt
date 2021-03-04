package com.hookiesolutions.webhookie.common.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpMethod

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 14:09
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CallbackDTO(
  @JsonProperty("id")
  val callbackId: String,
  val name: String,
  val httpMethod: HttpMethod,
  val url: String,
  val security: CallbackSecurityDTO?
) {
  val isSignable: Boolean
    get() = security != null
}
