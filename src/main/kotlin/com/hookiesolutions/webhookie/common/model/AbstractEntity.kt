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

package com.hookiesolutions.webhookie.common.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where

abstract class AbstractEntity : AbstractDocument() {
  @Id
  var id: String? = null

  @Suppress("unused")
  class Queries {
    companion object {
      fun byId(id: String?): Criteria {
        return where(UNDERSCORE_ID).`is`(id)
      }

      fun byObjectId(id: String): Criteria {
        return where(UNDERSCORE_ID).`is`(ObjectId(id))
      }

      fun idIsIn(ids: Collection<String>): Criteria {
        val objectIds = ids
          .filter { ObjectId.isValid(it) }
          .map { ObjectId(it) }
        return where(UNDERSCORE_ID).`in`(objectIds)
      }

      fun elemMatch(key: String, vararg criteria: Criteria): Criteria {
        val elemMatchCriteria = listOf(*criteria)
          .stream()
          .reduce(Criteria()) { l, r -> Criteria().andOperator(l, r) }

        return where(key).elemMatch(elemMatchCriteria)
      }

      fun regexField(key: String, value: String): Criteria {
        return where(key).regex(".*$value.*", "i")
      }

      fun regex(vararg pairs: Pair<String, String?>): Array<Criteria> {
        return pairs
          .filter { it.second != null }
          .map { regexField(it.first, it.second!!) }
          .toTypedArray()
      }

      fun filters(vararg pairs: Pair<String, Pair<String?,FieldMatchingStrategy>>): Array<Criteria> {
        return pairs
          .filter { it.second.first != null }
          .map {
            return@map if (it.second.second == FieldMatchingStrategy.PARTIAL_MATCH) {
              regexField(it.first, it.second.first!!)
            } else {
              where(it.first).`is`(it.second.first!!)
            }
          }
          .toTypedArray()
      }
    }
  }

  class Keys {
    companion object {
      val AGGREGATE_ROOT_FIELD = mongoField(mongoField("ROOT"))
    }
  }

  companion object {
    fun mongoField(name: String): String {
      return "${'$'}$name"
    }
  }
}
