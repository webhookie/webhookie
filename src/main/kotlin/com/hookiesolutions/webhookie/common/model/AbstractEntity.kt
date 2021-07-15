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
