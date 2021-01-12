package com.hookiesolutions.webhookie.portal.io

import amf.client.model.document.Document
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.codec.Encoder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.lang.Nullable
import org.springframework.stereotype.Component
import org.springframework.util.MimeType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.nio.charset.Charset

@Component
class AsyncDocumentEncoder: Encoder<Document>, HttpMessageWriter<Document> {
  override fun canEncode(elementType: ResolvableType, mimeType: MimeType?): Boolean {
    return elementType.isAssignableFrom(Document::class.java)
  }

  override fun encode(
    inputStream: Publisher<out Document>,
    dataBufferFactory: DataBufferFactory,
    resolvableType: ResolvableType,
    @Nullable mimeType: MimeType?,
    @Nullable hints: MutableMap<String, Any>?
  ): Flux<DataBuffer> {
    return Flux.from(inputStream)
      .map {
        val res = it.raw().orElse("")
        DefaultDataBufferFactory()
          .wrap(res.toByteArray(Charset.defaultCharset()))
      }
  }

  override fun getEncodableMimeTypes(): MutableList<MimeType> {
    return mutableListOf(
      MediaType.TEXT_PLAIN,
      MediaType.APPLICATION_JSON,
      AsyncDocumentReader.TEXT_YAML
    )
  }

  override fun getWritableMediaTypes(): MutableList<MediaType> {
    return mutableListOf(
      MediaType.TEXT_PLAIN,
      MediaType.APPLICATION_JSON,
      AsyncDocumentReader.TEXT_YAML
    )
  }

  override fun canWrite(elementType: ResolvableType, mediaType: MediaType?): Boolean {
    return elementType.isAssignableFrom(Document::class.java)
  }

  override fun write(
    inputStream: Publisher<out Document>,
    elementType: ResolvableType,
    mediaType: MediaType?,
    message: ReactiveHttpOutputMessage,
    hints: MutableMap<String, Any>
  ): Mono<Void> {
    return Mono.from(inputStream)
      .flatMap {
        val res = it.raw().orElse("")
        val body = DefaultDataBufferFactory().wrap(res.toByteArray(Charset.defaultCharset())).toMono()
        message.writeWith(body)
      }
  }
}