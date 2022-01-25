package com.hookiesolutions.webhookie.common.model

import javax.validation.constraints.Email

data class EmailValue(
  @Email
  val value: String
)
