package com.hookiesolutions.webhookie.common.config

import com.hookiesolutions.webhookie.common.annotation.ConditionalOnMissingEnvironmentVariable
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/5/21 13:05
 */
@Configuration
@ConditionalOnMissingEnvironmentVariable("WH_AMQP_HOST")
@EnableAutoConfiguration(exclude = [RabbitAutoConfiguration::class])
class ConditionalRabbitAutoConfiguration
