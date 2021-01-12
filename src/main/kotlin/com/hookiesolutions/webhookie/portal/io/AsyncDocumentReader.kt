package com.hookiesolutions.webhookie.portal.io

import amf.ProfileName
import amf.client.AMF
import amf.client.model.document.Document
import amf.client.parse.Async20Parser
import amf.client.validate.ValidationReport
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpInputMessage
import org.springframework.http.codec.HttpMessageReader
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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

    return DataBufferUtils.join(message.body)
      .flatMapMany { reader.bufferToDocument(it, charset) }
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

    return DataBufferUtils.join(message.body)
      .flatMap { reader.bufferToDocument(it, charset) }
  }

  companion object {
    const val TEXT_YAML_VALUE = "text/yaml"
    val TEXT_YAML = MediaType.valueOf(TEXT_YAML_VALUE)
  }
}

@Component
class AsyncSpecReader {
  private val parser = Async20Parser()

  init {
    AMF.init().get()
  }

  companion object {
    val ASYNC20_PROFILE: ProfileName = ProfileName.apply("Async20Profile")
  }

  fun bufferToDocument(buffer: DataBuffer, charset: Charset): Mono<Document> {
    return Mono.create {
      try {
        val msg = buffer.toString(charset)

        val model: Document = parser
          .parseStringAsync(msg)
          .get() as Document
        val v: ValidationReport = AMF.validate(model, ASYNC20_PROFILE) { ASYNC20_PROFILE }
          .get()

        if (v.conforms()) {
          it.success(model)
        } else {
          val errorMsg = v.results()
            .map { it.message() }
            .reduceRight { s, acc -> "$s\\n$acc" }
          it.error(IllegalArgumentException(errorMsg))
        }
      } catch (e: Exception) {
        it.error(IllegalArgumentException(e.localizedMessage, e))
      }
    }
  }
}
