package com.hookiesolutions.webhookie.portal.config

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
}