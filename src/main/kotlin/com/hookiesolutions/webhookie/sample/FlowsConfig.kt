package com.hookiesolutions.webhookie.sample

import com.hookiesolutions.webhookie.sample.model.Foo
import com.hookiesolutions.webhookie.sample.model.FooPublisherRepository
import org.slf4j.Logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.context.IntegrationFlowContext
import org.springframework.integration.webflux.dsl.WebFlux

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 30/11/20 16:45
 */
@Configuration
class FlowsConfig(
  private val log: Logger,
  private val repository: FooPublisherRepository,
  private val ctx: IntegrationFlowContext,
  private val factoryFoo: FooFlowFactory
) {
  @EventListener(ApplicationReadyEvent::class)
  fun loadFlows() {
    log.info("Loading flows....")

    repository.findAll()
      .filter { it.enabled }
      .doOnNext { log.info("Registering flow for path: '{}'", it.path) }
      .map { factoryFoo.generate(it) }
      .subscribe {
        ctx.registration(it).register()
      }
  }

  @Bean
  fun barFlow(): IntegrationFlow {
    val inboundGateway = WebFlux.inboundGateway("/bar")
      .autoStartup(true)
      .requestMapping {
        it
          .methods(HttpMethod.POST)
          .produces(MediaType.TEXT_PLAIN_VALUE)
          .consumes(MediaType.APPLICATION_JSON_VALUE)
      }
      .requestPayloadType(Foo::class.java)

    return IntegrationFlows.from(inboundGateway)
      .channel("serviceChannel")
      .get()!!
  }

}