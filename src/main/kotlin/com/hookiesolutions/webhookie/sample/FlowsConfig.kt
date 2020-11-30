package com.hookiesolutions.webhookie.sample

import com.hookiesolutions.webhookie.sample.model.FooPublisher
import com.hookiesolutions.webhookie.sample.model.FooPublisherRepository
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.client.model.changestream.OperationType
import org.slf4j.Logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.http.MediaType
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.mongodb.dsl.MongoDb
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 30/11/20 16:45
 */
@Configuration
class FlowsConfig(
  private val log: Logger,
  private val repository: FooPublisherRepository,
  private val mongoTemplate: ReactiveMongoTemplate,
  private val factoryFoo: FooFlowFactory,
  private val newPublisherChannel: MessageChannel
) {
  @EventListener(ApplicationReadyEvent::class)
  fun loadFlows() {
    log.info("Loading flows....")

    repository.findAll()
      .filter { it.enabled }
      .doOnNext { log.info("Registering flow for path: '{}'", it.path) }
      .map { MessageBuilder.withPayload(it).build() }
      .subscribe {
          newPublisherChannel.send(it)
      }
  }

  @Bean
  fun newEventsFlow(): IntegrationFlow {
    val match = Aggregation.match(Criteria.where("operationType").`is`(OperationType.INSERT.value))
    val changeStreamOptions = ChangeStreamOptions.builder()
      .fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
      .filter(Aggregation.newAggregation(match))
      .build()
    val producerSpec = MongoDb
      .changeStreamInboundChannelAdapter(mongoTemplate)
      .domainType(FooPublisher::class.java)
      .collection("fooPublisher")
      .options(changeStreamOptions)
      .extractBody(true)

    return IntegrationFlows.from(producerSpec)
      .transform<Message<FooPublisher>, FooPublisher> { it.payload }
      .channel(newPublisherChannel)
      .get()
  }

  @Bean
  fun handleNewPublisherFlow(): IntegrationFlow {
    return integrationFlow {
      channel(newPublisherChannel)
      handle { payload: FooPublisher, _: MessageHeaders ->
        factoryFoo.register(payload)
      }
    }
  }

  @Bean
  fun barFlow(): IntegrationFlow {
    return factoryFoo.generate(FooPublisher("/bar", "/bar", true, MediaType.APPLICATION_JSON_VALUE))
  }
}