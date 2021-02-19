package com.hookiesolutions.webhookie.common.web

import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 18/2/21 21:55
 */
@Configuration
class CommonAPIDocs {
  @Bean
  fun commonOpenApi(): GroupedOpenApi {
    val paths = arrayOf("${REQUEST_MAPPING_PUBLIC}/**")
    return GroupedOpenApi
      .builder()
      .group("Public")
      .pathsToMatch(*paths)
      .build()
  }

  companion object {
    const val REQUEST_MAPPING_PUBLIC = "/public"
  }
}
