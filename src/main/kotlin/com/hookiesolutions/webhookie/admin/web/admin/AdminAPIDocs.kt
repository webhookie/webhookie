package com.hookiesolutions.webhookie.admin.web.admin

import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 14/1/21 13:53
 */
@Configuration
class AdminAPIDocs {
  @Bean
  fun adminOpenApi(): GroupedOpenApi {
    val paths = arrayOf("$REQUEST_MAPPING_ADMIN/**")
    return GroupedOpenApi
      .builder()
      .group("Admin")
      .pathsToMatch(*paths)
      .build()
  }

  companion object {
    const val REQUEST_MAPPING_ADMIN = "/admin"
  }
}
