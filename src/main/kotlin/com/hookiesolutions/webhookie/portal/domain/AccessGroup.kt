package com.hookiesolutions.webhookie.portal.domain

import com.hookiesolutions.webhookie.portal.domain.AccessGroup.Keys.Companion.KEY_IAM_GROUP_NAME
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 14:05
 */
interface AccessGroup {
  val name: String
  val description: String
  val iamGroupName: String

  class Keys {
    companion object {
      const val KEY_IAM_GROUP_NAME = "iamGroupName"
    }
  }

  class Queries {
    companion object {
      fun iamGroupIs(name: String): Criteria {
        return where(KEY_IAM_GROUP_NAME).`is`(name)
      }
    }
  }
}