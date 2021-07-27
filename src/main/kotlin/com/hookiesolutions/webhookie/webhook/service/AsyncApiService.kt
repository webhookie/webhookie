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

package com.hookiesolutions.webhookie.webhook.service

import com.hookiesolutions.webhookie.common.exception.RemoteServiceException
import com.hookiesolutions.webhookie.webhook.service.model.AsyncApiSpec
import com.hookiesolutions.webhookie.webhook.service.model.WebhookApiRequest
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/5/21 02:33
 */
@Service
class AsyncApiService(
  private val parserWebClient: WebClient,
  private val log: Logger
) {
  fun parseAsyncApiSpecToWebhookApi(request: WebhookApiRequest): Mono<AsyncApiSpec> {
    return parserWebClient
      .post()
      .uri("/parse")
      .contentType(MediaType.parseMediaType("text/yaml"))
      .body(BodyInserters.fromValue(request.asyncApiSpec))
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(AsyncApiSpec::class.java)
      .doOnNext { log.info("AsyncAPI Spec parsed successfully. number of topics: '{}'", it.topics.size) }
      .doOnError { log.error("AsyncAPI Spec parse error '{}'", it) }
      .onErrorResume(WebClientRequestException::class.java) {
        RemoteServiceException("Unable to communicate to the spec parser! Please contact Administrator").toMono()
      }
  }
}
