package com.hookiesolutions.webhookie.consumer.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 00:29
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.consumer")
data class ConsumerProperties(
  val queue: String = "wh-customer.event",
  val missingHeader: ConsumerErrorExchangeProperties = ConsumerErrorExchangeProperties(
    exchange = "wh-customer",
    routingKey = "wh-missing-header"
  )
)

@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.consumer.missing-header")
data class ConsumerErrorExchangeProperties(
  val exchange: String,
  val routingKey: String
)
