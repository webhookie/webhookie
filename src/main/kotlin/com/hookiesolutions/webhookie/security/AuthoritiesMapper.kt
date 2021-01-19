package com.hookiesolutions.webhookie.security

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Component
class AuthoritiesMapper(
  private val securityProperties: WebHookieSecurityProperties
) {
  fun map(roles: List<String>): Set<GrantedAuthority> {
    return roles
      .asSequence()
      .map { securityProperties.roles.roleMapping.getOrDefault(it, it) }
      .plus(consumerAuthorityIfRequired())
      .map { SimpleGrantedAuthority(it) }
      .toSet()
  }

  private fun consumerAuthorityIfRequired(): List<String> {
    return if(securityProperties.roles.autoAssignConsumer) {
      listOf(ROLE_CONSUMER)
    } else {
      emptyList()
    }
  }
}