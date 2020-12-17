package com.hookiesolutions.webhookie.subscription.web.model

import com.hookiesolutions.webhookie.subscription.domain.Company
import javax.validation.constraints.NotEmpty

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:53
 */
data class CreateCompanyRequest(
  @field:NotEmpty
  val name: String
) {
  fun company(): Company {
    return Company(
      name
    )
  }
}
