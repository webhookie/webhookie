package com.hookiesolutions.webhookie.portal.io

import amf.client.model.document.Document
import org.springframework.core.ResolvableType
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpInputMessage
import org.springframework.http.codec.HttpMessageReader
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.nio.charset.Charset

@Component
class AsyncDocumentReader(
  private val reader: AsyncSpecReader
) : HttpMessageReader<Document> {
  fun getSupportedMediaTypes(): MutableList<MediaType> {
    return mutableListOf(
      MediaType.TEXT_PLAIN,
      MediaType.APPLICATION_JSON,
      TEXT_YAML
    )
  }

  override fun canRead(elementType: ResolvableType, mediaType: MediaType?): Boolean {
    return elementType.isAssignableFrom(Document::class.java)
  }

  override fun read(
    elementType: ResolvableType,
    message: ReactiveHttpInputMessage,
    hints: MutableMap<String, Any>
  ): Flux<Document> {
    val charset: Charset = message.headers.contentType?.charset ?: Charset.defaultCharset()

    return reader.read(message.body, charset)
      .toFlux()
  }

  override fun getReadableMediaTypes(): MutableList<MediaType> {
    return getSupportedMediaTypes()
  }

  override fun readMono(
    elementType: ResolvableType,
    message: ReactiveHttpInputMessage,
    hints: MutableMap<String, Any>
  ): Mono<Document> {
    val charset: Charset = message.headers.contentType?.charset ?: Charset.defaultCharset()

    return reader.read(message.body, charset)
      .toMono()
  }

  companion object {
    const val TEXT_YAML_VALUE = "text/yaml"
    val TEXT_YAML = MediaType.valueOf(TEXT_YAML_VALUE)
  }
}

