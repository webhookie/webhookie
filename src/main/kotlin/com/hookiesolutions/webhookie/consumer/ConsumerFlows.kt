package com.hookiesolutions.webhookie.consumer

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.consumer.config.ConsumerErrorExchangeProperties
import com.hookiesolutions.webhookie.consumer.config.ConsumerProperties
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.core.GenericSelector
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.Message
import org.springframework.messaging.SubscribableChannel
import org.springframework.retry.support.RetryTemplate

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:36
 */
@Configuration
@EnableConfigurationProperties(ConsumerProperties::class, ConsumerErrorExchangeProperties::class)
class ConsumerFlows(
  private val internalConsumerChannel: SubscribableChannel,
  private val consumerChannel: SubscribableChannel,
  private val missingHeadersChannel: SubscribableChannel,
  private val consumerProperties: ConsumerProperties,
  private val connectionFactory: ConnectionFactory,
  private val amqpTemplate: AmqpTemplate,
  private val consumerRetryTemplate: RetryTemplate,
  private val missingHeadersSelector: GenericSelector<Message<*>>,
) {
  @Bean
  fun consumerFlow(): IntegrationFlow {
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
  fun internalConsumerFlow(): IntegrationFlow {
    return integrationFlow {
      channel(internalConsumerChannel)
      transform<Message<ByteArray>> { ConsumerMessage.from(it) }
      channel(consumerChannel)
    }
  }

  @Bean
  fun missingHeadersFlow(): IntegrationFlow {
    val outboundAdapter = Amqp.outboundAdapter(amqpTemplate)
      .routingKey(consumerProperties.missingHeader.routingKey)
      .exchangeName(consumerProperties.missingHeader.exchange)
    return integrationFlow {
      channel(missingHeadersChannel)
      handle(outboundAdapter)
    }
  }
}