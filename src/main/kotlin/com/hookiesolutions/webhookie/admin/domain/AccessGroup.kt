/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

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

      fun iamGroupNameIs(group: String): Criteria {
        return where(KEY_IAM_GROUP_NAME).`is`(group)
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
