package com.hookiesolutions.webhookie.common.model.dto

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:31
 */
data class CallbackSecurityDTO(
  val method: String = "HMAC",
  val keyId: String
)
