package com.hookiesolutions.webhookie

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import reactor.tools.agent.ReactorDebugAgent

@SpringBootApplication
@EnableCaching
class WebhookieApplication

fun main(args: Array<String>) {
	ReactorDebugAgent.init()
	runApplication<WebhookieApplication>(*args)
}
