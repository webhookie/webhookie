package com.hookiesolutions.webhookie.subscription.service.security

import com.hookiesolutions.webhookie.subscription.domain.Application
import org.springframework.stereotype.Component

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/2/21 14:34
 */
@Component
class ApplicationAccessVoter {
  fun vote(application: Application, entity: String, groups: Collection<String>) : Boolean {
    return application.entity == entity &&
        groups.any { application.consumerIAMGroups.contains(it) }
  }
}