package com.hookiesolutions.webhookie.common.model.dto

import org.springframework.http.HttpMethod

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 01:25
 */
data class SubscriptionDTO(
  val id: String,
  val name: String,
  val topic: String,
  val callbackUrl: String,
  val httpMethod: HttpMethod,
  val callbackSecurity: CallbackSecurityDTO,
  val blockedDetails: BlockedDetailsDTO? = null
)
