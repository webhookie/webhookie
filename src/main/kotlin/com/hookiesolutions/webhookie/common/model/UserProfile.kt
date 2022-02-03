package com.hookiesolutions.webhookie.common.model

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class UserProfile(
  @field:NotBlank
  val name: String,
  @field:Email
  @field:NotBlank
  val email: String
)
