package com.hookiesolutions.webhookie

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.config.EnableIntegrationManagement
import org.springframework.integration.http.config.EnableIntegrationGraphController
import reactor.tools.agent.ReactorDebugAgent

@SpringBootApplication
@EnableCaching
@EnableIntegration
@EnableIntegrationManagement
@EnableIntegrationGraphController
class WebhookieApplication

fun main(args: Array<String>) {
	ReactorDebugAgent.init()
	runApplication<WebhookieApplication>(*args)
}
