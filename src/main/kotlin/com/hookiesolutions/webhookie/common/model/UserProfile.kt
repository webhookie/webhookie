package com.hookiesolutions.webhookie.common.model

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class UserProfile(
  @field:NotBlank
  val userId: String,
  val name: String?,
  @field:Email
  val email: String?
)
