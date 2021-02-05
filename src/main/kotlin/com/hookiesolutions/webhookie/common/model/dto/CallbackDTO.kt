package com.hookiesolutions.webhookie.common.model.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpMethod

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 14:09
 */
data class CallbackDTO(
  val id: String,
  val httpMethod: HttpMethod,
  val url: String,
  @JsonIgnore
  val isSignable: Boolean
)