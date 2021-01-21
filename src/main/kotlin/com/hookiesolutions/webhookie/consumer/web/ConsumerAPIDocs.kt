package com.hookiesolutions.webhookie.consumer.web

import com.hookiesolutions.webhookie.consumer.web.PublisherController.Companion.REQUEST_MAPPING_CONSUMER
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:43
 */
@Configuration
class ConsumerAPIDocs {
  @Bean
  fun consumerOpenApi(): GroupedOpenApi {
    val paths = arrayOf("${REQUEST_MAPPING_CONSUMER}/**")
    return GroupedOpenApi
      .builder()
      .group("Consumer")
      .pathsToMatch(*paths)
      .build()
  }
}