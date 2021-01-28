package com.hookiesolutions.webhookie.security.customizer

import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 15:25
 */
@Component
class AllowAllPermissionEvaluator: PermissionEvaluator {
  override fun hasPermission(authentication: Authentication?, targetDomainObject: Any?, permission: Any?): Boolean {
    return true
  }

  override fun hasPermission(
    authentication: Authentication?,
    targetId: Serializable?,
    targetType: String?,
    permission: Any?
  ): Boolean {
    return true
  }
}