package com.hookiesolutions.webhookie.portal.config

import amf.ProfileName
import amf.client.parse.Async20Parser
import amf.client.parse.Parser
import com.hookiesolutions.webhookie.portal.io.AsyncDocumentEncoder
import com.hookiesolutions.webhookie.portal.io.AsyncDocumentReader
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/1/21 15:34
 */
@Configuration
class PortalWebConfig {
  @Bean
  fun asyncCodecCustomizer(
    reader: AsyncDocumentReader,
    encoder: AsyncDocumentEncoder
  ): CodecCustomizer {
    return CodecCustomizer {
      it.customCodecs().register(reader)
      it.customCodecs().register(encoder)
    }
  }

  @Bean
  fun async20Parser(): Parser {
    return Async20Parser()
  }

  @Bean
  fun asyncProfileName(): ProfileName {
    return ProfileName.apply("Async20Profile")
  }
}