package com.hookiesolutions.webhookie.admin.domain

import com.hookiesolutions.webhookie.admin.domain.AccessGroup.Keys.Companion.KEY_ENABLED
import com.hookiesolutions.webhookie.admin.domain.AccessGroup.Keys.Companion.KEY_IAM_GROUP_NAME
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.inValues


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 14:05
 */
abstract class AccessGroup: AbstractEntity() {
  abstract val name: String
  abstract val description: String
  abstract val iamGroupName: String
  abstract val enabled: Boolean

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
      const val KEY_IAM_GROUP_NAME = "iamGroupName"
      const val KEY_ENABLED = "enabled"
    }
  }
}