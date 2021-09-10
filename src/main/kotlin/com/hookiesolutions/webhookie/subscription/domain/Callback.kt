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

import com.fasterxml.jackson.annotation.JsonInclude
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO
import com.hookiesolutions.webhookie.subscription.domain.Callback.Keys.Companion.KEY_APPLICATION_ID
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.http.HttpMethod

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/2/21 17:27
 */
@Document(collection = "callback")
@TypeAlias("callback")
@CompoundIndexes(
  CompoundIndex(
    name = "callback_applicationId_request_target",
    def = "{'applicationId' : 1, 'httpMethod' : 1, 'url': 1}",
    unique = true
  ),
  CompoundIndex(
    name = "callback_name_application",
    def = "{'applicationId' : 1, 'name': 1}",
    unique = true
  )
)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Callback(
  val name: String,
  val applicationId: String,
  val httpMethod: HttpMethod,
  val url: String,
  val security: CallbackSecurity? = null,
): AbstractEntity() {
  fun requestTarget(): String {
    return "${httpMethod.name} $url"
  }

  fun details() = CallbackDetails(id!!, name, httpMethod, url, security)

  fun dto() = CallbackDTO(id!!, name, httpMethod, url, security?.dto())

  class Queries {
    companion object {
      fun applicationIdIs(id: String): Criteria {
        return where(KEY_APPLICATION_ID).`is`(id)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_APPLICATION_ID = "applicationId"
    }
  }
}

