package com.hookiesolutions.webhookie

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import reactor.tools.agent.ReactorDebugAgent

@Configuration
@Profile("dev")
class DebugAgentConfig {
  @EventListener(ApplicationReadyEvent::class)
  fun initForDebug() {
    ReactorDebugAgent.init()
  }
}
