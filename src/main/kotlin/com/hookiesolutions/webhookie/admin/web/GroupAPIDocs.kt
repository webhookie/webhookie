package com.hookiesolutions.webhookie.admin.web

import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 11:36
 */
@Configuration
class GroupAPIDocs {
  @Bean
  fun groupOpenApi(): GroupedOpenApi {
    val paths = arrayOf("${REQUEST_MAPPING_GROUP}/**")
    return GroupedOpenApi
      .builder()
      .group("Groups")
      .pathsToMatch(*paths)
      .build()
  }

  companion object {
    const val REQUEST_MAPPING_GROUP = "/group"
  }
}
