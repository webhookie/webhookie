package com.hookiesolutions.webhookie.subscription.service.model.subscription

import com.hookiesolutions.webhookie.common.model.UserProfile
import javax.validation.constraints.Email

data class UserProfileRequest(
  val name: String?,
  @field:Email
  val email: String?
) {
  fun toUserProfile(userId: String): UserProfile {
    return UserProfile(userId, name, email)
  }
}
