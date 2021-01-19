package com.hookiesolutions.webhookie.portal.domain.group

import com.hookiesolutions.webhookie.portal.domain.group.AccessGroup.Keys.Companion.KEY_DESCRIPTION
import com.hookiesolutions.webhookie.portal.domain.group.AccessGroup.Keys.Companion.KEY_ENABLED
import com.hookiesolutions.webhookie.portal.domain.group.AccessGroup.Keys.Companion.KEY_IAM_GROUP_NAME
import com.hookiesolutions.webhookie.portal.domain.group.AccessGroup.Keys.Companion.KEY_NAME
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.inValues


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 14:05
 */
interface AccessGroup {
  var id: String?
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

  class Queries {
    companion object {
      fun iamGroupNameIn(groups: Collection<String>): Criteria {
        return Criteria()
          .andOperator(
            where(KEY_IAM_GROUP_NAME).inValues(groups),
            isEnabled()
          )
      }

      private fun isEnabled(): Criteria {
        return where(KEY_ENABLED).`is`(true)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_NAME = "name"
      const val KEY_DESCRIPTION = "description"
      const val KEY_IAM_GROUP_NAME = "iamGroupName"
      const val KEY_ENABLED = "enabled"
    }
  }
}