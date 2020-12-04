package com.hookiesolutions.webhookie.subscription.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import org.slf4j.Logger
import org.springframework.http.MediaType

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/12/20 15:49
 */
class Convertors(
  private val log: Logger,
  private val om: ObjectMapper
) {
  fun a(cm: ConsumerMessage) {
    log.info("{}", cm)

    val messageAsString = String(cm.payload)
    log.info("{}", messageAsString)

    val mapTypeRef = MapTypeRef()

    if(cm.contentType == MediaType.APPLICATION_JSON_VALUE) {
      try {
        val map: Map<String, Any> = om.readValue(cm.payload, mapTypeRef) as Map<String, Any>
        println(map)
      } catch (e: Exception) {
        log.error(e.message)
      }
    } else if (cm.contentType == MediaType.APPLICATION_XML_VALUE) {
      try {
        val xm = XmlMapper()
        val map: Map<String, Any> = xm.readValue(cm.payload, mapTypeRef) as Map<String, Any>
        println(map)
      } catch (e: Exception) {
        log.error(e.message)
      }
    }
  }
}

class MapTypeRef: TypeReference<Map<String, Any>>()