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

package com.hookiesolutions.webhookie.ingress

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.ingress.config.IngressProperties
import com.hookiesolutions.webhookie.ingress.service.TrafficServiceDelegate
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
class IngressFlows(
  private val internalIngressChannel: SubscribableChannel,
  private val ingressChannel: SubscribableChannel,
  private val missingHeadersChannel: SubscribableChannel,
  private val ingressProperties: IngressProperties,
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

            internalIngressChannel.send(message)
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
      channel(internalIngressChannel)
      transform<Message<ByteArray>> { toConsumerMessageTransformer.transform(it) }
      channel(ingressChannel)
    }
  }

  @Bean
  @ConditionalOnBean(AmqpTemplate::class)
  fun missingHeadersFlow(amqpTemplate: AmqpTemplate, ): IntegrationFlow {
    val outboundAdapter = Amqp.outboundAdapter(amqpTemplate)
      .routingKey(ingressProperties.missingHeader.routingKey)
      .exchangeName(ingressProperties.missingHeader.exchange)
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
      .inboundGateway(connectionFactory, amqpTemplate, ingressProperties.queue)
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
        recipient(internalIngressChannel) { msg: Message<*> ->
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
