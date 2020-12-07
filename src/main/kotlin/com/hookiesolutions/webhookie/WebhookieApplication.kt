package com.hookiesolutions.webhookie

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.tools.agent.ReactorDebugAgent

@SpringBootApplication
class WebhookieApplication

fun main(args: Array<String>) {
	ReactorDebugAgent.init()
	runApplication<WebhookieApplication>(*args)
}
