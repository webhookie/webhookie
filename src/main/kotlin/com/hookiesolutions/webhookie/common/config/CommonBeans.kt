package com.hookiesolutions.webhookie.common.config

import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.core.GenericSelector

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/6/21 15:28
 */
@Configuration
class CommonBeans {
  @Bean
  fun retryableErrorSelector(): GenericSelector<GenericPublisherMessage> {
    return GenericSelector {
      it is PublisherRequestErrorMessage || (
          it is PublisherResponseErrorMessage && (
              it.response.is5xxServerError() || it.response.isNotFound()
              )
          )
    }
  }
}
