package com.hookiesolutions.webhookie.portal.io

import amf.ProfileName
import amf.client.AMF
import amf.client.model.document.Document
import amf.client.parse.Async20Parser
import amf.client.validate.ValidationReport
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpInputMessage
import org.springframework.http.codec.HttpMessageReader
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.nio.charset.Charset

@Component
class AsyncDocumentReader: HttpMessageReader<Document> {
  fun getSupportedMediaTypes(): MutableList<MediaType> {
    return mutableListOf(
      MediaType.TEXT_PLAIN,
      MediaType.APPLICATION_JSON,
      MediaType.valueOf("application/yml")
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

    return message.body
      .map { bufferToDocument(it, charset)}
  }

  fun bufferToDocument(buffer: DataBuffer, charset: Charset): Document {
    return try {
      val msg = buffer.toString(charset)
      val model: Document = Async20Parser()
        .parseStringAsync(msg)
        .get() as Document
      val profileName = ProfileName.apply("Async20Profile")
      val v: ValidationReport = AMF.validate(model, profileName) { profileName }
        .get()

      if(v.conforms()) {
        model
      } else {
        val errorMsg = v.results()
          .map { it.message() }
          .reduceRight { s, acc -> "$s\\n$acc"}
        throw IllegalArgumentException(errorMsg)
      }
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
  ): Mono<Document> {
    val charset: Charset = message.headers.contentType?.charset ?: Charset.defaultCharset()

    return message.body.toMono()
      .map { bufferToDocument(it, charset) }
  }
}