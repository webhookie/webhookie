package com.hookiesolutions.webhookie.common.model

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class EmailValue(
  @field:Email
  @field:NotBlank
  val value: String
)
