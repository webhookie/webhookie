package com.hookiesolutions.webhookie.subscription.enterprise.config

import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.OAuth2CallbackAuthorizer
import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.exception.SubscriptionAuthorizationException
import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.model.message.OAuthSignedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ErrorMessage
import org.springframework.messaging.support.MessageBuilder

@Configuration
class OAuth2Flows(
  private val log: Logger
) {
  @Bean
  fun oAuth2AuthorizeSubscriptionFlow(
    signSubscriptionMessageChannel: MessageChannel,
    subscriptionAuthorizationErrorChannel: MessageChannel,
    subscriptionService: SubscriptionService,
    oauth2CallbackAuthorizer: OAuth2CallbackAuthorizer,
    subscriptionChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(signSubscriptionMessageChannel)
      enrichHeaders {
        defaultOverwrite(true)
        errorChannel(subscriptionAuthorizationErrorChannel)
      }
      filter<Message<SignableSubscriptionMessage>> { it.payload.subscription.callback.securityScheme?.isOAuth() ?: false }
      transform<Message<SignableSubscriptionMessage>> { m ->
        subscriptionService.subscriptionById(m.payload.subscriptionId)
          .flatMap { subscription ->
            oauth2CallbackAuthorizer.authorize(subscription.callback)
              .map { token ->
                OAuthSignedSubscriptionMessage.Builder()
                  .message(m.payload)
                  .token(token)
                  .build()
              }
          }
          .onErrorMap { SubscriptionAuthorizationException(it.localizedMessage, m.payload, m.headers) }
      }
      split()
      channel(subscriptionChannel)
    }
  }

  @Bean
  fun subscriptionAuthorizationErrorHandler(
    subscriptionAuthorizationErrorChannel: MessageChannel,
    unsuccessfulSubscriptionChannel: MessageChannel,
    globalSubscriptionErrorChannel: MessageChannel,
  ): IntegrationFlow {
    return integrationFlow {
      channel(subscriptionAuthorizationErrorChannel)
      routeToRecipients {
        recipientFlow<ErrorMessage>({ it.payload.cause is SubscriptionAuthorizationException }) {
          transform<ErrorMessage> {
            log.error("Subscription Authorization error: '{}'", it.payload.cause!!.localizedMessage)
            val cause = it.payload.cause as SubscriptionAuthorizationException
            val payload = PublisherResponseErrorMessage(cause.subscriptionMessage, cause.response, cause.message)
            MessageBuilder
              .withPayload(payload)
              .copyHeaders(cause.headers)
              .build()
          }
          channel(unsuccessfulSubscriptionChannel)
        }
        recipientFlow<ErrorMessage>({ it.payload.cause !is SubscriptionAuthorizationException }) {
          handle {
            log.error("Unexpected error occurred handling message: '{}', '{}", it.payload, it.headers)
          }
        }
      }
    }
  }
}
