package com.hookiesolutions.webhookie.subscription.service.model

import com.hookiesolutions.webhookie.subscription.domain.Application
import javax.validation.constraints.NotEmpty

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 22:45
 */
data class CreateApplicationRequest(
  @field:NotEmpty
  val name: String
) {
  fun createApplicationFor(companyId: String): Application {
    return Application(name, companyId)
  }
}
