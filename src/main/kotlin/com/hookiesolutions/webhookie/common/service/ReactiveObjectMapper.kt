package com.hookiesolutions.webhookie.common.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ReactiveObjectMapper(
  private val objectMapper: ObjectMapper
) {
  fun <T> readValue(value: String, clazz: Class<T>): Mono<T> {
    return Mono.create { sink ->
      try {
        sink.success(objectMapper.readValue(value, clazz))
      } catch (ex: Exception) {
        sink.error(ex)
      }
    }
  }

  fun <T> writeValueAsString(any: T): Mono<String> {
    return Mono.create { sink ->
      try {
        sink.success(objectMapper.writeValueAsString(any))
      } catch (ex: Exception) {
        sink.error(ex)
      }
    }
  }
}