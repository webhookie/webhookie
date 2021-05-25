package com.hookiesolutions.webhookie.common.web

import com.hookiesolutions.webhookie.security.TokenData

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 11:52
 */
data class UserResponse(
  val entity: String,
  val consumerGroups: List<String>,
  val providerGroups: List<String>,
  val roles: List<String>,
  val email: String
) {
  companion object {
    fun from(
      data: TokenData,
      consumerGroups: MutableList<String>,
      providerGroups: MutableList<String>
    ): UserResponse {
      val myConsumerGroups = data.groups.filter { consumerGroups.contains(it) }
      val myProviderGroups = data.groups.filter { providerGroups.contains(it) }
      return UserResponse(
        data.entity, myConsumerGroups, myProviderGroups, data.roles.map { it.authority }, data.email
      )
    }
  }
}
