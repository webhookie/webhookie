package com.hookiesolutions.webhookie.subscription.web

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
      "${REQUEST_MAPPING_APPLICATIONS}/**",
      "${REQUEST_MAPPING_SUBSCRIPTIONS}/**",
    )
    return GroupedOpenApi
      .builder()
      .group("Subscription")
      .pathsToMatch(*paths)
      .build()
  }

  companion object {
    const val REQUEST_MAPPING_APPLICATIONS = "/applications"
    const val REQUEST_MAPPING_CALLBACKS = "/callbacks"
    const val REQUEST_MAPPING_SUBSCRIPTIONS = "/subscriptions"
  }

}