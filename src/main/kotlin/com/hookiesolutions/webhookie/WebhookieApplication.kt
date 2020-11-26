package com.hookiesolutions.webhookie

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebhookieApplication

fun main(args: Array<String>) {
	runApplication<WebhookieApplication>(*args)
}
