/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.consumer.config

import com.hookiesolutions.webhookie.common.Constants
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_ALL_HEADERS
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_REQUIRED_HEADERS
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.service.IdGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.core.GenericSelector
import org.springframework.integration.transformer.GenericTransformer
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 03:11
 */
@Configuration
class ConsumerConfig(private val idGenerator: IdGenerator) {
  @Bean
  fun traceIdExtractor(): GenericTransformer<Message<*>, String?> {
    return GenericTransformer {
      it.headers[WH_HEADER_TRACE_ID] as String?
    }
  }

  @Bean
  fun toWebhookieHeadersTransformer(): GenericTransformer<Message<ByteArray>, MessageHeaders> {
    return GenericTransformer { message ->
      MessageHeaders(message.headers.filter { WH_ALL_HEADERS.contains(it.key) })
    }
  }

  @Bean
  fun toConsumerMessageTransformer(): GenericTransformer<Message<ByteArray>, ConsumerMessage> {
    return GenericTransformer { message ->
      val topic = message.headers[Constants.Queue.Headers.WH_HEADER_TOPIC] as String
      val contentType = message.headers[Constants.Queue.Headers.HEADER_CONTENT_TYPE] as String

      @Suppress("UNCHECKED_CAST")
      val authorizedSubscribers: Collection<String> =
        message.headers[Constants.Queue.Headers.WH_HEADER_AUTHORIZED_SUBSCRIBER] as? Collection<String> ?: emptySet()

      val headerTraceId = message.headers[WH_HEADER_TRACE_ID] as String?
      val traceId = headerTraceId ?: idGenerator.generate()
      ConsumerMessage(
        traceId,
        topic,
        contentType,
        authorizedSubscribers.toSet(),
        message.payload,
        message.headers
      )
    }
  }

  @Bean
  fun missingHeadersSelector(): GenericSelector<Message<*>> {
    return GenericSelector {
      !it.headers.keys
        .containsAll(WH_REQUIRED_HEADERS)
    }
  }

  @Bean
  fun consumerRetryTemplate(): RetryTemplate {
    val retryTemplate = RetryTemplate()
    val fixedBackOffPolicy = FixedBackOffPolicy()
    fixedBackOffPolicy.backOffPeriod = 2000L
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy)
    val retryPolicy = SimpleRetryPolicy()
    retryPolicy.maxAttempts = 2
    retryTemplate.setRetryPolicy(retryPolicy)
    return retryTemplate
  }
}
