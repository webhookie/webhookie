package com.hookiesolutions.webhookie.portal.domain

import com.hookiesolutions.webhookie.portal.domain.AccessGroup.Keys.Companion.KEY_DESCRIPTION
import com.hookiesolutions.webhookie.portal.domain.AccessGroup.Keys.Companion.KEY_IAM_GROUP_NAME
import com.hookiesolutions.webhookie.portal.domain.AccessGroup.Keys.Companion.KEY_NAME
import org.springframework.data.mongodb.core.query.Update


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 14:05
 */
interface AccessGroup {
  val name: String
  val description: String
  val iamGroupName: String
  val enabled: Boolean

  class Updates {
    companion object {
      fun updateGroupDetails(newGroup: AccessGroup): Update {
        return Update()
          .set(KEY_NAME, newGroup.name)
          .set(KEY_DESCRIPTION, newGroup.description)
          .set(KEY_IAM_GROUP_NAME, newGroup.iamGroupName)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_NAME = "name"
      const val KEY_DESCRIPTION = "description"
      const val KEY_IAM_GROUP_NAME = "iamGroupName"
    }
  }
}