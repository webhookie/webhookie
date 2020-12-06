package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.subscription.domain.Company.Keys.Companion.KEY_COMPANY_COLLECTION_NAME
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

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
  class Keys {
    companion object {
      const val KEY_SUBSCRIPTIONS = "subscriptions"

      const val KEY_COMPANY_COLLECTION_NAME = "company"
    }
  }
}
