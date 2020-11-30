package com.hookiesolutions.webhookie.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where

abstract class AbstractEntity : AbstractDocument() {
  @Id
  var id: String? = null

  @Suppress("unused")
  class Queries {
    companion object {
      fun byId(id: String?): Criteria {
        return where("_id").`is`(id)
      }
    }
  }
}