package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
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
  @Indexed(unique = true)
  val name: String,
  val description: String? = null,
  @Indexed
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
      const val APPLICATION_COLLECTION_NAME = "application"
    }
  }
}
