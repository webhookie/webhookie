package com.hookiesolutions.webhookie.consumer.web

import com.hookiesolutions.webhookie.consumer.web.PublisherController.Companion.CONSUMER_REQUEST_MAPPING
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
    val paths = arrayOf("${CONSUMER_REQUEST_MAPPING}/**")
    return GroupedOpenApi
      .builder()
      .group("consumer")
      .pathsToMatch(*paths)
      .build()
  }
}