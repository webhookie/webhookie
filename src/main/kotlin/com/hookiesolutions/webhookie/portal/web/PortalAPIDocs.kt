package com.hookiesolutions.webhookie.portal.web

import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 14/1/21 13:53
 */
@Configuration
class PortalAPIDocs {
  @Bean
  fun portalOpenApi(): GroupedOpenApi {
    val paths = arrayOf("${REQUEST_MAPPING_PORTAL_ADMIN}/**")
    return GroupedOpenApi
      .builder()
      .group("Admin Portal")
      .pathsToMatch(*paths)
      .build()
  }

  companion object {
    const val REQUEST_MAPPING_PORTAL_ADMIN = "/portal/admin"
  }
}