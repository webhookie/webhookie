package com.hookiesolutions.webhookie.common.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 12/1/21 12:21
 */
@Service
class YamlService {
  fun read(yaml: String): Mono<Map<String, Any>> {
    return Mono.create {
      try {
        val objectMapper = ObjectMapper(YAMLFactory())
        val typeReference = object : TypeReference<Map<String, Any>>() {}
        val map = objectMapper.readValue(yaml, typeReference)

        it.success(map)
      } catch (e: Exception) {
        it.error(IllegalArgumentException(e.localizedMessage, e))
      }
    }
  }
}