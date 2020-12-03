package com.hookiesolutions.webhookie.subscription.web.model

import com.hookiesolutions.webhookie.subscription.domain.Company
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import org.bson.types.ObjectId
import javax.validation.constraints.NotEmpty

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:53
 */
data class CreateCompanyRequest(
  @field:NotEmpty
  val name: String,
  val subscriptions: Set<Subscription>
) {
  fun company(): Company {

    subscriptions.forEach {
      it.id = ObjectId.get().toHexString()
    }
    return Company(
      name, subscriptions
    )
  }
}
