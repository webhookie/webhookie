package com.hookiesolutions.webhookie.security

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
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
) {
  fun hasAdminAuthority(): Boolean {
    return roles.map { it.authority }.contains(ROLE_ADMIN)
  }
}
