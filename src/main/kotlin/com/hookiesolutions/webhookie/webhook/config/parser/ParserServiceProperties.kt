package com.hookiesolutions.webhookie.webhook.config.parser

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/5/21 01:18
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.asyncapi.parser")
data class ParserServiceProperties(
  val url: String = "http://localhost:3000"
)

