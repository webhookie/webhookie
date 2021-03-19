package com.hookiesolutions.webhookie.audit.web

import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 16/3/21 11:16
 */
@Configuration
class TrafficAPIDocs {
  @Bean
  fun trafficOpenApi(): GroupedOpenApi {
    val paths = arrayOf("${REQUEST_MAPPING_TRAFFIC}/**")
    return GroupedOpenApi
      .builder()
      .group("Traffic")
      .pathsToMatch(*paths)
      .build()
  }

  companion object {
    const val REQUEST_MAPPING_TRAFFIC = "/traffic"
  }
}
