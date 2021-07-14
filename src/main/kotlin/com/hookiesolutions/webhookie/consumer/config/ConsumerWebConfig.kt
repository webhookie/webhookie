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
