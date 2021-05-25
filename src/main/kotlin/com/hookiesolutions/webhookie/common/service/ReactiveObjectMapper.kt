package com.hookiesolutions.webhookie.common.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ReactiveObjectMapper(
  private val objectMapper: ObjectMapper
) {
  fun <K,V> readMap(value: String): Mono<Map<K,V>> {
    val typeReference = object : TypeReference<Map<K,V>>() {}
    return readValue(value, typeReference)
  }

  fun <T> readValue(value: String, valueTypeRef: TypeReference<T>): Mono<T> {
    return Mono.create { sink ->
      try {
        sink.success(objectMapper.readValue(value, valueTypeRef))
      } catch (ex: Exception) {
        sink.error(ex)
      }
    }
  }
}
