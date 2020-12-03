package com.hookiesolutions.webhookie.consumer.config

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_REQUIRED_HEADERS
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.core.GenericSelector
import org.springframework.messaging.Message
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 03:11
 */
@Configuration
class ConsumerConfig {
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

/*
  @Bean
  fun eventPublisherChannelFlow(
    connectionFactory: ConnectionFactory,
    amqpTemplate: AmqpTemplate
  ): IntegrationFlow {
    val outboundGateway = Amqp.outboundAdapter(amqpTemplate)
      .routingKey("wh-event")
      .exchangeName("wh-customer")
    return IntegrationFlows
      .from(eventPublisherChannel)
      .log<Message<*>> { log.info("{}", it) }
      .handle(outboundGateway)
      .nullChannel()
  }

  @Bean
  fun container(
    connectionFactory: ConnectionFactory,
  ): SimpleMessageListenerContainer {
    val container = SimpleMessageListenerContainer()
    container.connectionFactory = connectionFactory
    container.setQueueNames("wh-customer.event")
    return container
  }

  @Bean("wh-customer.event.dlq")
  fun dlq(): Queue {
    return QueueBuilder.durable("wh-customer.event.dlq")
      .build()
  }

  @Bean("DLX.exchange")
  fun dlqExchange(): DirectExchange {
    return DirectExchange("DLX", true, false)
  }

  @Bean("wh-customer.event.dlq.binding")
  fun dlqBinding(dlqExchange: DirectExchange): Binding {
    return Binding(dlq().name, Binding.DestinationType.QUEUE, dlqExchange.name, "wh-event", emptyMap())
  }

  @ServiceActivator(inputChannel = "customerEventInChannel", outputChannel = "subscriptionInChannel")
  fun eventFlowActivator(
    message: Message<*>,
    @Header(WH_HEADER_TOPIC, required = true) topic: String,
    @Header(WH_HEADER_TRACE_ID, required = true) traceId: String,
    @Header(HEADER_CONTENT_TYPE, required = true) contentType: String,
    @Header(WH_HEADER_AUTHORIZED_SUBSCRIBER, required = false, defaultValue = "") authorizedSubscribers: List<String> = emptyList()
  ): Message<*> {
    log.info("{}", message.payload)
    log.info("{}", message.headers)
    log.info("{}", topic)

    return message
  }

  @Bean
  fun customerEventConsumer(): Consumer<Message<Any>> {
    return Consumer {
      log.info("{}", it)
    }
  }
*/
}