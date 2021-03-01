package com.hookiesolutions.webhookie.security

import org.springframework.security.core.GrantedAuthority

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/2/21 02:17
 */
data class TokenData(
  val entity: String,
  val groups: List<String>,
  val roles: Collection<GrantedAuthority>,
  val email: String
)
