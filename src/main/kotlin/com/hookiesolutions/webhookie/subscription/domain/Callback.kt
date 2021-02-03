package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.subscription.domain.Callback.Keys.Companion.KEY_APPLICATION_ID
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/2/21 17:27
 */
@Document(collection = "callback")
@TypeAlias("callback")
@CompoundIndexes(
  CompoundIndex(
    name = "request_target",
    def = "{'details.httpMethod' : 1, 'details.url': 1}",
    unique = true
  )
)
data class Callback(
  val name: String,
  val applicationId: String,
  val details: CallbackDetails,
): AbstractEntity() {
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

