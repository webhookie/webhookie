package com.hookiesolutions.webhookie.subscription.service.model

import com.hookiesolutions.webhookie.common.validation.IRI
import com.hookiesolutions.webhookie.subscription.domain.CallbackSecurity
import org.springframework.http.HttpMethod
import javax.validation.constraints.NotBlank

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 23:22
 */
data class CreateSubscriptionRequest(
  @field:NotBlank
  val name: String,
  @field:NotBlank
  val topic: String,
  @field:IRI(message = "Invalid callbackUrl!")
  val callbackUrl: String,
  val httpMethod: HttpMethod,
  val callbackSecurity: CallbackSecurity?
)