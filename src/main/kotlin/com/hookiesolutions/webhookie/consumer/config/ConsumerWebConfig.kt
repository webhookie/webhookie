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

package com.hookiesolutions.webhookie.consumer.config

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.HEADER_CONTENT_TYPE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_ALL_HEADERS
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID_MISSING
import com.hookiesolutions.webhookie.common.service.IdGenerator
import org.springframework.context.annotation.Configuration
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpInputMessage
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.nio.charset.Charset

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 14/7/21 15:08
 */
@Configuration
class ConsumerWebConfig(
  private val reader: MessageReader
): WebFluxConfigurer {
/*
  @Bean
  fun asyncCodecCustomizer(reader: MessageReader,
  ): CodecCustomizer {
    return CodecCustomizer {
      it.customCodecs().register(reader)
    }
  }
*/

  override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
    configurer.customCodecs().register(reader)
  }
}

@Component
class MessageReader(
  private val idGenerator: IdGenerator,
): HttpMessageReader<Message<*>> {
  fun getSupportedMediaTypes(): MutableList<MediaType> {
    return mutableListOf(
      MediaType.ALL
    )
  }

  override fun canRead(elementType: ResolvableType, mediaType: MediaType?): Boolean {
    return Message::class.java.isAssignableFrom(elementType.toClass())
  }

  override fun read(
    elementType: ResolvableType,
    message: ReactiveHttpInputMessage,
    hints: MutableMap<String, Any>
  ): Flux<Message<*>> {
    val charset: Charset = message.headers.contentType?.charset ?: Charset.defaultCharset()

    return message.body
      .map { bufferToDocument(it, charset, message.headers)}
  }

  fun bufferToDocument(buffer: DataBuffer, charset: Charset, headers: HttpHeaders): Message<*> {
    return try {
      val msg = buffer.toString(charset).toByteArray(charset)
      val validHeaders = headers.toSingleValueMap()
        .filter { WH_ALL_HEADERS.contains(it.key) }
      val builder = MessageBuilder
        .withPayload(msg)
        .copyHeaders(validHeaders)
        .setHeaderIfAbsent(HEADER_CONTENT_TYPE, headers.contentType?.toString() ?: "")
      if(validHeaders[WH_HEADER_TRACE_ID] == null) {
        builder.setHeader(WH_HEADER_TRACE_ID, idGenerator.generate())
        builder.setHeader(WH_HEADER_TRACE_ID_MISSING, "true")
      }

      builder.build()
    } catch (e: Exception) {
      throw IllegalArgumentException(e.localizedMessage, e)
    }
  }

  override fun getReadableMediaTypes(): MutableList<MediaType> {
    return getSupportedMediaTypes()
  }

  override fun readMono(
    elementType: ResolvableType,
    message: ReactiveHttpInputMessage,
    hints: MutableMap<String, Any>
  ): Mono<Message<*>> {
    val charset: Charset = message.headers.contentType?.charset ?: Charset.defaultCharset()

    return message.body.toMono()
      .map { bufferToDocument(it, charset, message.headers) }
  }
}
