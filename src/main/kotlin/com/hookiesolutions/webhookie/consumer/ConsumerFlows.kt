package com.hookiesolutions.webhookie.consumer

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.consumer.config.ConsumerProperties
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.core.GenericSelector
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.transformer.GenericTransformer
import org.springframework.messaging.Message
import org.springframework.messaging.SubscribableChannel
import org.springframework.retry.support.RetryTemplate


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:36
 */
@Configuration
class ConsumerFlows(
  private val internalConsumerChannel: SubscribableChannel,
  private val consumerChannel: SubscribableChannel,
  private val missingHeadersChannel: SubscribableChannel,
  private val consumerProperties: ConsumerProperties,
  private val consumerRetryTemplate: RetryTemplate,
  private val missingHeadersSelector: GenericSelector<Message<*>>,
) {
  @Bean
  @ConditionalOnBean(AmqpTemplate::class)
  fun connectionFactory(
    properties: RabbitProperties
  ): ConnectionFactory {
    val connectionFactory = com.rabbitmq.client.ConnectionFactory()
    connectionFactory.connectionTimeout = properties.connectionTimeout.toMillis().toInt()
    connectionFactory.host = properties.host
    connectionFactory.port = properties.port
    return CachingConnectionFactory(
      connectionFactory
    )
  }

  @Bean
  @ConditionalOnBean(AmqpTemplate::class)
  fun consumerFlow(
    connectionFactory: ConnectionFactory,
    amqpTemplate: AmqpTemplate,
  ): IntegrationFlow {
    val inboundGateway = Amqp
      .inboundGateway(connectionFactory, amqpTemplate, consumerProperties.queue)
      .retryTemplate(consumerRetryTemplate)
    return IntegrationFlows
      .from(inboundGateway)
      .routeToRecipients {
        it
          .recipient(missingHeadersChannel, missingHeadersSelector)
          .defaultOutputChannel(internalConsumerChannel)
      }
      .get()
  }

  @Bean
  fun internalConsumerFlow(toConsumerMessageTransformer: GenericTransformer<Message<ByteArray>, ConsumerMessage>): IntegrationFlow {
    return integrationFlow {
      channel(internalConsumerChannel)
      transform<Message<ByteArray>> { toConsumerMessageTransformer.transform(it) }
      channel(consumerChannel)
    }
  }

  @Bean
  @ConditionalOnBean(AmqpTemplate::class)
  fun missingHeadersFlow(
    amqpTemplate: AmqpTemplate,
  ): IntegrationFlow {
    val outboundAdapter = Amqp.outboundAdapter(amqpTemplate)
      .routingKey(consumerProperties.missingHeader.routingKey)
      .exchangeName(consumerProperties.missingHeader.exchange)
    return integrationFlow {
      channel(missingHeadersChannel)
      handle(outboundAdapter)
    }
  }
}
