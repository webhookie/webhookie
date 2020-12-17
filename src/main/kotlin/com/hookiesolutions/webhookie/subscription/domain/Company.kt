package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractDocument.Keys.Companion.KEY_VERSION
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.elemMatch
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.subscription.domain.Company.Keys.Companion.KEY_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_BLOCK_DETAILS
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Update

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:26
 */
@Document(collection = "company")
data class Company(
  @Indexed(unique = true)
  val name: String,
  val subscriptions: Set<Subscription>
) : AbstractEntity() {
  fun findSubscriptionByUpdateRegex(regexList: List<String>?): Subscription? {
    val idx = regexList
      .orEmpty()
      .filter { key -> key.startsWith(KEY_SUBSCRIPTIONS) }
      .map { key -> Regex("$KEY_SUBSCRIPTIONS\\.(\\d+)\\.$KEY_BLOCK_DETAILS").find(key) }
      .asSequence()
      .map { it!!.groupValues[1] }
      .map { it.toInt() }
      .firstOrNull()

    return if((idx != null) && idx < subscriptions.size) {
      subscriptions.toList()[idx]
    } else {
      null
    }
  }

  class Queries {
    companion object {
      fun bySubscriptionId(id: String): Criteria {
        return elemMatch(
          KEY_SUBSCRIPTIONS,
          byId(id)
        )
      }
    }
  }

  class Updates {
    companion object {
      fun blockSubscription(details: BlockedDetailsDTO): Update {
        return Update()
          .set("$KEY_SUBSCRIPTIONS.$.$KEY_BLOCK_DETAILS", details)
          .inc(KEY_VERSION, 1)
      }

      fun unblockSubscription(): Update {
        return Update()
          .unset("$KEY_SUBSCRIPTIONS.$.$KEY_BLOCK_DETAILS")
          .inc(KEY_VERSION, 1)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_SUBSCRIPTIONS = "subscriptions"
    }
  }
}
