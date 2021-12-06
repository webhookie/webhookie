package com.hookiesolutions.webhookie.instance.migration.versions.v110

import com.bol.crypt.CryptVault
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacDetails
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacSecurityScheme
import com.hookiesolutions.webhookie.subscription.service.converter.CallbackSecretConverter
import com.mongodb.BasicDBObject
import org.bson.BSONCallback
import org.bson.BSONObject
import org.bson.BasicBSONDecoder
import org.bson.types.Binary
import org.slf4j.Logger
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class SecretConverter(
  private val log: Logger,
  private val cryptVault: CryptVault
): Converter<Binary, HmacSecurityScheme> {
  override fun convert(source: Binary): HmacSecurityScheme {
    return try {
      val decrypted = cryptVault.decrypt(source.data)

      val decoder = BasicBSONDecoder()
      val bsonCallback: BSONCallback = CallbackSecretConverter.BasicDBObjectCallback()
      decoder.decode(decrypted, bsonCallback)
      val deserialized = bsonCallback.get() as BSONObject
      val obj = deserialized.get("") as BasicDBObject
      val keyId = obj.get(HmacDetails.PROPERTY_KEY_ID) as String
      val secret = obj.get(HmacDetails.PROPERTY_SECRET) as String

      HmacSecurityScheme(HmacDetails(keyId, secret))

    } catch (ex: Exception) {
      log.warn("Unable to read Secret from Binary!, returning an EMPTY ( invalid ) Secret to be used")
      HmacSecurityScheme(HmacDetails("", ""))
    }
  }
}
