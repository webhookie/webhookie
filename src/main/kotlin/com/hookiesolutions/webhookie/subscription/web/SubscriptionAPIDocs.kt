package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.subscription.web.ApplicationController.Companion.REQUEST_MAPPING_APPLICATION
import com.hookiesolutions.webhookie.subscription.web.CompanyController.Companion.REQUEST_MAPPING_COMPANY
import com.hookiesolutions.webhookie.subscription.web.SubscriptionController.Companion.REQUEST_MAPPING_SUBSCRIPTION
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:45
 */
@Configuration
class SubscriptionAPIDocs {
  @Bean
  fun subscriptionOpenApi(): GroupedOpenApi {
    val paths = arrayOf(
      "${REQUEST_MAPPING_COMPANY}/**",
      "${REQUEST_MAPPING_APPLICATION}/**",
      "${REQUEST_MAPPING_SUBSCRIPTION}/**",
    )
    return GroupedOpenApi
      .builder()
      .group("Subscription")
      .pathsToMatch(*paths)
      .build()
  }
}