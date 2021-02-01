package com.hookiesolutions.webhookie.subscription.service.model

import javax.validation.constraints.NotBlank

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 22:45
 */
data class ApplicationRequest(
  @field:NotBlank
  val name: String,
  val consumerGroups: Set<String>
)
