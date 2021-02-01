package com.hookiesolutions.webhookie.subscription.service.model

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 22:45
 */
data class ApplicationRequest(
  @field:NotBlank
  val name: String,
  val description: String? = null,
  @field:NotEmpty
  val consumerGroups: Set<String>
)
