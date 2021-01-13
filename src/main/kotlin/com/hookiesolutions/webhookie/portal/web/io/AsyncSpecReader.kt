package com.hookiesolutions.webhookie.portal.web.io

import amf.ProfileName
import amf.client.AMF
import amf.client.model.document.Document
import amf.client.parse.Parser
import amf.client.validate.ValidationReport
import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.Charset

@Component
class AsyncSpecReader(
  private val parser: Parser,
  private val asyncProfileName: ProfileName
) {
  fun read(bufferFlux: Flux<DataBuffer>, charset: Charset): Publisher<Document> {
    return DataBufferUtils.join(bufferFlux)
      .map { it.toString(charset) }
      .flatMap { readDocument(it) }
      .flatMap { validate(it)}
      .onErrorMap {
        IllegalArgumentException(it.localizedMessage, it)
      }
  }

  fun readDocument(value: String): Mono<Document> {
    return Mono.create {
      try {
        val document = parser
          .parseStringAsync(value)
          .get() as Document

        it.success(document)
      } catch (e: Exception) {
        it.error(e)
      }
    }
  }

  fun validate(document: Document): Mono<Document> {
    return Mono.create { sink ->
      try {
        val validationReport: ValidationReport = AMF.validate(document, asyncProfileName) { asyncProfileName }
          .get()

        if (validationReport.conforms()) {
          sink.success(document)
        } else {
          val errorMsg = validationReport.results()
            .map { it.message() }
            .reduceRight { s, acc -> "$s\\n$acc" }
          sink.error(IllegalArgumentException(errorMsg))
        }
      } catch (e: Exception) {
        sink.error(e)
      }
    }
  }

  init {
    AMF.init().get()
  }
}