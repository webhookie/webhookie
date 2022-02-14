/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.config.EnableIntegrationManagement
import java.util.*


@SpringBootApplication
@EnableCaching
@EnableIntegration
@EnableIntegrationManagement
@EnableReactiveMongoAuditing
@ConfigurationPropertiesScan
class WebhookieApplication

fun readApplicationVersion(app: SpringApplication): String {
  var version = "UNKNOWN"
  try {
    val buildPropertiesFile = "META-INF/build-info.properties"
    val versionKey = "build.version"

    val inputStream = app.classLoader.getResourceAsStream(buildPropertiesFile)
    val buildProperties = Properties()
    buildProperties.load(inputStream)
    version = buildProperties[versionKey] as String
  } catch (_: Exception) {
  }

  return version
}

fun main(args: Array<String>) {
  runApplication<WebhookieApplication>(*args) {
    this.setBanner(WebhookieBanner(readApplicationVersion(this)))
  }
}

