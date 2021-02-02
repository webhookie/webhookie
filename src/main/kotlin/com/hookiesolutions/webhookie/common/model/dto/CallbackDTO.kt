package com.hookiesolutions.webhookie.common.model.dto

import org.springframework.http.HttpMethod

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 14:09
 */
data class CallbackDTO(
  val url: String,
  val httpMethod: HttpMethod,
  val isSignable: Boolean
)