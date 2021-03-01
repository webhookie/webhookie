package com.hookiesolutions.webhookie.common.web

import com.hookiesolutions.webhookie.security.TokenData

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 11:52
 */
data class UserResponse(
  val entity: String,
  val groups: List<String>,
  val roles: List<String>,
  val email: String
) {
  companion object {
    fun from(data: TokenData): UserResponse {
      return UserResponse(
        data.entity, data.groups, data.roles.map { it.authority }, data.email
      )
    }
  }
}
