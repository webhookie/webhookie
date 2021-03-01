package com.hookiesolutions.webhookie.subscription.config

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Callback
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:33
 */
@Configuration
class SubscriptionMongoConfig {
  @Bean
  fun subscriptionIndexEntities(): List<Class<out AbstractEntity>> =
    listOf(
      BlockedSubscriptionMessage::class.java,
      Application::class.java,
      Callback::class.java,
      Subscription::class.java
    )
}
