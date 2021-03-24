package com.hookiesolutions.webhookie

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.config.EnableIntegrationManagement

@SpringBootApplication
@EnableCaching
@EnableIntegration
@EnableIntegrationManagement
@EnableReactiveMongoAuditing
@ConfigurationPropertiesScan
class WebhookieApplication

fun main(args: Array<String>) {
  runApplication<WebhookieApplication>(*args) {
    this.setBanner(WebhookieBanner())
  }
}

