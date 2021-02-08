package com.hookiesolutions.webhookie.common.model.dto

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/2/21 14:37
 */
data class CallbackSecurityDTO(
  val method: String = "HMAC",
  val keyId: String
)
