package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractDocument.Keys.Companion.KEY_VERSION
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.elemMatch
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.subscription.domain.Company.Keys.Companion.KEY_COMPANY_COLLECTION_NAME
import com.hookiesolutions.webhookie.subscription.domain.Company.Keys.Companion.KEY_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_BLOCK_DETAILS
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Update
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:26
 */
@Document
@TypeAlias(KEY_COMPANY_COLLECTION_NAME)
data class Company(
  @Indexed(unique = true)
  val name: String,
  val subscriptions: Set<Subscription>
): AbstractEntity() {
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
    }
  }

  class Keys {
    companion object {
      const val KEY_SUBSCRIPTIONS = "subscriptions"

      const val KEY_COMPANY_COLLECTION_NAME = "company"
    }
  }
}
