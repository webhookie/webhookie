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

package com.hookiesolutions.webhookie.analytics.service

import com.hookiesolutions.webhookie.analytics.config.AnalyticsProperties
import com.hookiesolutions.webhookie.analytics.domain.InstanceRepository
import com.hookiesolutions.webhookie.analytics.domain.WebhookieInstance
import com.hookiesolutions.webhookie.analytics.service.model.AnalyticsData
import com.hookiesolutions.webhookie.analytics.service.model.RemoteInstance
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.utils.CryptoUtils
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.util.function.Consumer

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/8/21 15:59
 */
@Service
class AnalyticsService(
  @Qualifier("analyticsClient")
  private val analyticsClient: WebClient,
  private val log: Logger,
  private val idGenerator: IdGenerator,
  private val timeMachine: TimeMachine,
  private val analyticsServerBaseUrl: String,
  private val analyticsProperties: AnalyticsProperties,
  private val repository: InstanceRepository
) {
  @EventListener(ApplicationReadyEvent::class)
  fun initAnalytics() {
    val successHandler: Consumer<WebhookieInstance> = Consumer {
      log.info("Using Webhookie instance : {}", it)
    }
    val errorHandler: Consumer<Throwable> = Consumer {
      log.error("Failed to create/load webhookie instance due to: '{}'",  it.localizedMessage)
    }
    readOrCreateInstance()
      .subscribe(successHandler, errorHandler)
  }

  fun readOrCreateInstance(): Mono<WebhookieInstance> {
    return repository.findOne()
      .switchIfEmpty { createInstance() }
  }

  fun createInstance(): Mono<WebhookieInstance> {
    return createRemoteInstance()
      .flatMap { saveInstance(it) }
      .flatMap { updateInstance(it) }
  }

  fun createRemoteInstance(): Mono<RemoteInstance> {
    log.info("Webhookie instance does not exist! creating one...")
    val time = timeMachine.now().toString()
    val requestId = idGenerator.generate()
    val signatureValue = "(request-target): POST $analyticsServerBaseUrl/instances x-date: $time x-request-id: $requestId"

    return CryptoUtils.hmac(signatureValue, analyticsProperties.apiKey)
      .flatMap { sig ->
        val h = "keyId=1,algorithm=${CryptoUtils.ALG},headers=(request-target) x-date x-request-id,signature=$sig"
        analyticsClient
          .post()
          .uri("/instances")
          .contentType(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, "Signature $h")
          .header("x-date", time)
          .header("x-request-id", requestId)
          .accept(MediaType.ALL)
          .retrieve()
          .bodyToMono(RemoteInstance::class.java)
          .doOnNext { log.debug("Remote Instance was created successfully with id: '{}'", it.id) }
          .doOnError { log.warn("Was unable to create Remote instance due to: '{}'", it.localizedMessage) }
      }
  }

  fun updateInstance(instance: WebhookieInstance): Mono<WebhookieInstance> {
    val opt = if(analyticsProperties.send) {
      AnalyticsOpt.IN
    } else {
      AnalyticsOpt.OUT
    }

    val uri = "/instances/${instance.instanceId}/${opt.name.lowercase()}"

    log.info("Webhookie instance does not exist! creating one...")
    val time = timeMachine.now().toString()
    val requestId = idGenerator.generate()
    val signatureValue = "(request-target): POST $analyticsServerBaseUrl$uri x-date: $time x-request-id: $requestId"

    return CryptoUtils.hmac(signatureValue, instance.apiKey)
      .flatMap { sig ->
        val h = "algorithm=${CryptoUtils.ALG},headers=(request-target) x-date x-request-id,signature=$sig"
        analyticsClient
          .post()
          .uri(uri)
          .header(HttpHeaders.AUTHORIZATION, "Signature $h")
          .header("x-date", time)
          .header("x-request-id", requestId)
          .accept(MediaType.ALL)
          .retrieve()
          .bodyToMono(String::class.java)
          .doOnNext { log.debug("Remote Instance was updated successfully with result: '{}'", it, instance.instanceId) }
          .doOnError { log.warn("Was unable to update Remote instance due to: '{}'", it.localizedMessage) }
      }
      .map { instance }
  }

  fun saveInstance(remoteInstance: RemoteInstance): Mono<WebhookieInstance> {
    val instanceId = remoteInstance.id
    log.info("Saving Webhookie instance: {}", instanceId)
    val instance = WebhookieInstance(instanceId, remoteInstance.apiKey, "http://localhost:7070")
    return repository.save(instance)
      .doOnNext { log.debug("Local Instance was created successfully with id: '{}'", it.instanceId) }
  }

  fun sendData(data: AnalyticsData): Mono<String> {
    return readOrCreateInstance()
      .flatMap { postData(it, data)}
      .switchIfEmpty("Instance does not exist!".toMono())
  }

  private fun postData(instance: WebhookieInstance, data: AnalyticsData): Mono<String> {
    log.info("Sending Webhookie instance data....")
    val time = timeMachine.now().toString()
    val requestId = idGenerator.generate()
    val signatureValue = "(request-target): " +
        "POST $analyticsServerBaseUrl/instances/${instance.instanceId} " +
        "x-date: $time " +
        "x-from: ${data.from} " +
        "x-to: ${data.to} " +
        "x-items: ${data.items.hashCode()} " +
        "x-request-id: $requestId"

    return CryptoUtils.hmac(signatureValue, instance.apiKey)
      .flatMap { sig ->
        val h = "keyId=1,algorithm=${CryptoUtils.ALG},headers=(request-target) x-date x-from x-to x-items x-request-id,signature=$sig"
        analyticsClient
          .post()
          .uri("/instances/${instance.instanceId}")
          .contentType(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, "Signature $h")
          .header("x-date", time)
          .header("x-from", data.from.toString())
          .header("x-to", data.to.toString())
          .header("x-items", data.items.hashCode().toString())
          .header("x-request-id", requestId)
          .accept(MediaType.ALL)
          .body(BodyInserters.fromValue(data))
          .retrieve()
          .bodyToMono(String::class.java)
      }
  }
}

enum class AnalyticsOpt {
  IN,
  OUT
}
