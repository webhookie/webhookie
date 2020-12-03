package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.subscription.web.CompanyController.Companion.COMPANY_REQUEST_MAPPING
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
    val paths = arrayOf("${COMPANY_REQUEST_MAPPING}/**")
    return GroupedOpenApi
      .builder()
      .group("subscription")
      .pathsToMatch(*paths)
      .build()
  }
}