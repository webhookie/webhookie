package com.hookiesolutions.webhookie.publisher.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 9/12/20 20:40
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.publisher")
data class PublisherProperties(
  val maxRetry: Int = 10
)
