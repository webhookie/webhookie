package com.hookiesolutions.webhookie.consumer

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.consumer.config.ConsumerProperties
import com.hookiesolutions.webhookie.consumer.service.TrafficServiceDelegate
import org.slf4j.Logger
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.context.IntegrationContextUtils
import org.springframework.integration.core.GenericSelector
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.transformer.GenericTransformer
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.support.MessageBuilder
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
  private val traceIdExtractor: GenericTransformer<Message<*>, String?>,
  private val toWebhookieHeadersTransformer: GenericTransformer<Message<ByteArray>, MessageHeaders>,
  private val traceServiceDelegate: TrafficServiceDelegate,
  private val log: Logger,
) {
  @ConditionalOnBean(AmqpTemplate::class)
  @RabbitListener(queues = ["${'$'}{webhookie.consumer.queue:wh-customer.event}"])
  fun handleIncomingMessage(msg: Message<ByteArray>) {
    if (missingHeadersSelector.accept(msg)) {
      missingHeadersChannel.send(msg)
    } else {
      val traceId = traceIdExtractor.transform(msg)
      traceServiceDelegate.checkOrGenerateTrace(traceId)
        .subscribe(
          {
            val message = MessageBuilder
              .withPayload(msg.payload)
              .copyHeaders(toWebhookieHeadersTransformer.transform(msg))
              .setHeader(WH_HEADER_TRACE_ID, it)
              .build()

            internalConsumerChannel.send(message)
          },
          {
            log.warn("Message was rejected due to duplicate traceId '{}'", traceId)
          }
        )
    }
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
  fun missingHeadersFlow(amqpTemplate: AmqpTemplate, ): IntegrationFlow {
    val outboundAdapter = Amqp.outboundAdapter(amqpTemplate)
      .routingKey(consumerProperties.missingHeader.routingKey)
      .exchangeName(consumerProperties.missingHeader.exchange)
    return integrationFlow {
      channel(missingHeadersChannel)
      handle(outboundAdapter)
    }
  }

  //  @Bean
  @ConditionalOnBean(AmqpTemplate::class)
  @Suppress("unused")
  fun consumerFlow(
    connectionFactory: ConnectionFactory,
    amqpTemplate: AmqpTemplate,
  ): IntegrationFlow {
    val inboundGateway = Amqp
      .inboundGateway(connectionFactory, amqpTemplate, consumerProperties.queue)
      .retryTemplate(consumerRetryTemplate)
    return integrationFlow(inboundGateway) {
      enrichHeaders {
        defaultOverwrite(true)
        replyChannel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME, true)
      }
      routeToRecipients {
        recipient(missingHeadersChannel) { msg: Message<*> ->
          missingHeadersSelector.accept(msg)
        }
        recipient(internalConsumerChannel) { msg: Message<*> ->
          !missingHeadersSelector.accept(msg)
        }
      }
    }
  }

//  @Bean
  @ConditionalOnBean(AmqpTemplate::class)
  @Suppress("unused")
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
}
