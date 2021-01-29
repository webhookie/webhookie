package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.Callback
import com.hookiesolutions.webhookie.subscription.domain.Application.Keys.Companion.KEY_ENTITY
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 16:26
 */
@Document(collection = "application")
@TypeAlias("application")
data class Application(
  @Indexed(unique = true)
  val name: String,
  @Indexed
  val entity: String,
  val callback: Callback
): AbstractEntity() {
  class Queries {
    companion object {
      fun applicationsByEntity(entity: String): Criteria {
        return where(KEY_ENTITY).`is`(entity)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_ENTITY = "entity"
    }
  }
}
