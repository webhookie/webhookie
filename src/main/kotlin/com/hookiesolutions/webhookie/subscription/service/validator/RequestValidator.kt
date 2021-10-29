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

package com.hookiesolutions.webhookie.subscription.service.validator

import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.service.model.CallbackValidationSampleRequest
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriUtils
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/2/21 15:17
 */
@Service
class RequestValidator(
  private val log: Logger,
  private val timeMachine: TimeMachine
) {
  fun validateRequest(sampleRequest: CallbackValidationSampleRequest): Mono<ResponseEntity<ByteArray>> {
    val decodedUrl = UriUtils.decode(sampleRequest.url, StandardCharsets.UTF_8)
    log.info("Validating request to '{}'", sampleRequest.callback.requestTarget())
    return WebClient
      .create(decodedUrl)
      .method(sampleRequest.httpMethod)
      .accept(MediaType.ALL)
      .body(BodyInserters.fromValue(sampleRequest.payload.encodeToByteArray()))
      .headers { sampleRequest.addMessageHeaders(it, timeMachine.now()) }
      .retrieve()
      .toEntity(ByteArray::class.java)
      .doOnNext { log.debug("Received '{}' response", it.statusCode) }
      .map {
        return@map if(it.hasBody()) {
          ResponseEntity
            .ok()
            .headers(it.headers)
            .body(it.body)
        } else {
          ResponseEntity
            .ok()
            .headers(it.headers)
            .build()
        }
      }
      .doOnError { log.warn("Validation Failed: '{}'", it.localizedMessage) }
  }
}
