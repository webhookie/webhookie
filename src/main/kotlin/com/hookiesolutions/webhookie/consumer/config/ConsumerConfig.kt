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
