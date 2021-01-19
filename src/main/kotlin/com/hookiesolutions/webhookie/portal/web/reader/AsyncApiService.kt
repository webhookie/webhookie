package com.hookiesolutions.webhookie.portal.web.reader

import amf.client.model.document.Document
import amf.plugins.domain.webapi.models.EndPoint
import com.hookiesolutions.webhookie.common.service.YamlService
import com.hookiesolutions.webhookie.portal.domain.Topic
import com.hookiesolutions.webhookie.portal.service.model.AsyncApiSpec
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.Objects
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 12/1/21 11:40
 */
@Service
class AsyncApiService(
  private val yamlService: YamlService
) {
  fun readMono(body: Document): Mono<AsyncApiSpec> {
    val name: String? = readString(body, "core#name")
    val version: String? = readString(body, "core#version")
    val description: String? = readString(body, "core#description")
    val topics: List<Topic> = readList(body, "apiContract#endpoint") {
      val endPoint = it as EndPoint
      Topic(endPoint.path().value(), endPoint.description().value())
    }

    return when {
      name.isNullOrEmpty() -> {
        Mono.error(IllegalArgumentException("WebhookGroup's name cannot be empty"))
      }
      version.isNullOrEmpty() -> {
        Mono.error(IllegalArgumentException("WebhookGroup's version cannot be empty"))
      }
      topics.isEmpty() -> {
        Mono.error(IllegalArgumentException("WebhookGroup's topics cannot be empty"))
      }
      else -> {
        val yaml = body.raw().get()
        yamlService.read(yaml)
          .map {
            AsyncApiSpec(name, version, description, topics, yaml, it)
          }
          .toMono()
      }
    }
  }

  fun <T> readValues(body: Document, name: String, mapper: Function<Any, T>): Stream<T> {
    val key = "$PREFIX$name"
    val graph = body.encodes().graph()

    if(!graph.properties().contains(key)) {
      return Stream.empty()
    }

    return graph
      .scalarByProperty("$PREFIX$name")
      .stream()
      .map(mapper)
  }

  fun <T> readFrom(body: Document, name: String, mapper: Function<Any, T>): T? {
    val value = readValues(body, name, mapper)
      .findFirst()

    return if(value.isPresent) {
      value.get()
    } else {
      null
    }
  }

  fun readString(body: Document, name: String): String? {
    return readFrom(body, name, Objects::toString)
  }

  fun <T> readList(body: Document, name: String, mapper: Function<Any, T>): List<T> {
    return readValues(body, name, mapper)
      .collect(Collectors.toList())
  }

  companion object {
    const val PREFIX = "http://a.ml/vocabularies/"
  }
}

