package com.hookiesolutions.webhookie.portal.web.io

import com.hookiesolutions.webhookie.common.service.ReactiveObjectMapper
import com.hookiesolutions.webhookie.portal.domain.ConsumerAccess
import com.hookiesolutions.webhookie.portal.domain.ProviderAccess
import com.hookiesolutions.webhookie.portal.service.model.AsyncApiSpec
import com.hookiesolutions.webhookie.portal.service.model.WebhookGroupRequest
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpInputMessage
import org.springframework.http.codec.HttpMessageReader
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import java.nio.charset.Charset

@Component
class WebhookGroupRequestReader(
  private val reader: AsyncSpecReader,
  private val service: AsyncApiService,
  private val objectMapper: ReactiveObjectMapper
) : HttpMessageReader<WebhookGroupRequest> {
  fun getSupportedMediaTypes(): MutableList<MediaType> {
    return mutableListOf(MediaType.APPLICATION_JSON)
  }

  override fun canRead(elementType: ResolvableType, mediaType: MediaType?): Boolean {
    return (elementType.resolve() == WebhookGroupRequest::class.java) &&
        getSupportedMediaTypes().contains(mediaType)
  }

  override fun read(
    elementType: ResolvableType,
    message: ReactiveHttpInputMessage,
    hints: MutableMap<String, Any>
  ): Flux<WebhookGroupRequest> {
    return innerRead(message)
      .toFlux()
  }

  override fun getReadableMediaTypes(): MutableList<MediaType> {
    return getSupportedMediaTypes()
  }

  override fun readMono(
    elementType: ResolvableType,
    message: ReactiveHttpInputMessage,
    hints: MutableMap<String, Any>
  ): Mono<WebhookGroupRequest> {
    return innerRead(message)
      .toMono()
  }

  private fun innerRead(message: ReactiveHttpInputMessage): Publisher<WebhookGroupRequest> {
    val charset: Charset = message.headers.contentType?.charset ?: Charset.defaultCharset()

    return DataBufferUtils.join(message.body)
      .map { it.toString(charset) }
      .flatMap { objectMapper.readValue(it, InnerWebhookGroupRequest::class.java) }
      .flatMap(this::extractSpec)
      .map(this::toWebhookGroupRequest)
      .onErrorMap { IllegalArgumentException(it.localizedMessage) }
  }

  private fun extractSpec(innerRequest: InnerWebhookGroupRequest): Mono<Tuple2<InnerWebhookGroupRequest, AsyncApiSpec>> {
    return specMono(innerRequest)
      .flatMap { reader.read(it) }
      .flatMap { reader.validate(it) }
      .flatMap { Mono.zip(innerRequest.toMono(), service.readMono(it)) }
  }

  private fun specMono(innerRequest: InnerWebhookGroupRequest): Mono<String> {
    return if (innerRequest.spec is String) {
      innerRequest.spec.toMono()
    } else {
      objectMapper
        .writeValueAsString(innerRequest.spec)
    }
  }

  private fun toWebhookGroupRequest(it: Tuple2<InnerWebhookGroupRequest, AsyncApiSpec>): WebhookGroupRequest {
    val innerRequest = it.t1
    return WebhookGroupRequest(
      it.t2,
      innerRequest.consumerGroups,
      innerRequest.providerGroups,
      innerRequest.consumerAccess,
      innerRequest.providerAccess
    )
  }

  data class InnerWebhookGroupRequest(
    val spec: Any,
    val consumerGroups: List<String>,
    val providerGroups: List<String>,
    val consumerAccess: ConsumerAccess,
    val providerAccess: ProviderAccess,
  )
}

