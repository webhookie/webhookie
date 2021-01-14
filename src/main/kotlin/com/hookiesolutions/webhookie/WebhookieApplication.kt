package com.hookiesolutions.webhookie

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.config.EnableIntegrationManagement
import reactor.tools.agent.ReactorDebugAgent

@SpringBootApplication
@EnableCaching
@EnableIntegration
@EnableIntegrationManagement
@EnableReactiveMongoAuditing
class WebhookieApplication

fun main(args: Array<String>) {
  runApplication<WebhookieApplication>(*args)
}

@Configuration
@Profile("dev")
class DebugAgentConfig {
  @EventListener(ApplicationReadyEvent::class)
  fun initForDebug() {
    ReactorDebugAgent.init()
  }
}