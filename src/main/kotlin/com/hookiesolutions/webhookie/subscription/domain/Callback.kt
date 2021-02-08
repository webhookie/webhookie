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
    name = "request_target",
    def = "{'httpMethod' : 1, 'url': 1}",
    unique = true
  ),
  CompoundIndex(
    name = "name_application",
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

