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

package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.subscription.domain.Application.Keys.Companion.APPLICATION_COLLECTION_NAME
import com.hookiesolutions.webhookie.subscription.domain.Application.Keys.Companion.KEY_CONSUMER_IAM_GROUPS
import com.hookiesolutions.webhookie.subscription.domain.Application.Keys.Companion.KEY_ENTITY
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Update

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 16:26
 */
@Document(collection = APPLICATION_COLLECTION_NAME)
@TypeAlias("application")
data class Application(
  @Indexed(name = "application.name")
  val name: String,
  val description: String? = null,
  @Indexed(name = "application.entity")
  val entity: String,
  val consumerIAMGroups: Set<String>
): AbstractEntity() {
  fun details(): ApplicationDetails {
    return ApplicationDetails(id!!, name, entity)
  }

  class Queries {
    companion object {
      fun applicationConsumerGroupsIn(groups: Collection<String>): Criteria {
        return where(KEY_CONSUMER_IAM_GROUPS).`in`(groups)
      }

      fun applicationConsumerGroupsIs(value: String): Criteria {
        return where(KEY_CONSUMER_IAM_GROUPS).`is`(value)
      }

      fun applicationsByEntity(entity: String): Criteria {
        return where(KEY_ENTITY).`is`(entity)
      }
    }
  }

  class Updates {
    companion object {
      fun pullConsumerGroup(value: String): Update {
        return Update()
          .pull(KEY_CONSUMER_IAM_GROUPS, value)
      }

      fun setConsumerGroup(value: String): Update {
        return Update()
          .set("$KEY_CONSUMER_IAM_GROUPS.$", value)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_ENTITY = "entity"
      const val KEY_CONSUMER_IAM_GROUPS = "consumerIAMGroups"
      const val KEY_MANAGES = "manages"
      const val APPLICATION_COLLECTION_NAME = "application"
    }
  }
}
