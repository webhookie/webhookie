package com.hookiesolutions.webhookie.subscription.enterprise.config

import com.hookiesolutions.webhookie.common.exception.messaging.SubscriptionMessageHandlingException
import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.model.message.OAuthSignedSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.OAuth2CallbackAuthorizer
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import com.hookiesolutions.webhookie.subscription.service.factory.ConversionsFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.transformer.GenericTransformer
import org.springframework.messaging.MessageChannel
import reactor.core.publisher.Mono

@Configuration
class OAuth2Flows {
  @Bean
  fun oauth2AuthorizeSubscriptionMessage(
    subscriptionService: SubscriptionService,
    oauth2CallbackAuthorizer: OAuth2CallbackAuthorizer,
    factory: ConversionsFactory
  ): GenericTransformer<SignableSubscriptionMessage, Mono<OAuthSignedSubscriptionMessage>> {
    return GenericTransformer { msg ->
      subscriptionService.subscriptionById(msg.subscriptionId)
        .flatMap { subscription ->
          oauth2CallbackAuthorizer.authorize(subscription.callback)
            .map { token ->
              OAuthSignedSubscriptionMessage(
                originalMessage = msg.originalMessage,
                spanId = msg.spanId,
                subscription = msg.subscription,
                delay = msg.delay,
                numberOfRetries = msg.numberOfRetries,
                totalNumberOfTries = msg.totalNumberOfTries,
                token = token,
              )
            }
        }
        .onErrorMap { SubscriptionMessageHandlingException(it.localizedMessage, msg.traceId, msg.spanId) }
    }
  }

  @Bean
  fun oAuth2AuthorizeSubscriptionFlow(
    oauth2AuthorizeSubscriptionMessage: GenericTransformer<SignableSubscriptionMessage, Mono<OAuthSignedSubscriptionMessage>>,
    signSubscriptionMessageChannel: MessageChannel,
    subscriptionChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(signSubscriptionMessageChannel)
      filter<SignableSubscriptionMessage> { it.subscription.callback.securityScheme?.isOAuth() ?: false }
      transform(oauth2AuthorizeSubscriptionMessage)
      split()
      channel(subscriptionChannel)
    }
  }

}
