package com.hookiesolutions.webhookie.subscription.config

import com.bol.config.EncryptAutoConfiguration
import com.hookiesolutions.webhookie.subscription.service.CallbackSecretConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 31/12/20 17:15
 */
@Configuration
@Import(EncryptAutoConfiguration::class)
class SubscriptionConvertersConfig {
  @Bean
  fun mongoCustomConversions(
    callbackSecretConverter: CallbackSecretConverter
  ): MongoCustomConversions {
    return MongoCustomConversions.create { it.registerConverter(callbackSecretConverter) }
  }
}
