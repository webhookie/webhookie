package com.hookiesolutions.webhookie.subscription.service.converter

import com.bol.crypt.CryptVault
import com.hookiesolutions.webhookie.subscription.domain.Callback
import com.hookiesolutions.webhookie.subscription.domain.CallbackSecurity
import com.hookiesolutions.webhookie.subscription.domain.Secret
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import org.bson.BSONCallback
import org.bson.BSONObject
import org.bson.BasicBSONCallback
import org.bson.BasicBSONDecoder
import org.bson.BasicBSONEncoder
import org.bson.BasicBSONObject
import org.bson.Document
import org.bson.types.Binary
import org.slf4j.Logger
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/1/21 12:01
 */
@Component
class CallbackSecretConverter(
  private val log: Logger,
  private val cryptVault: CryptVault
): Converter<Binary, Secret> {
  override fun convert(source: Binary): Secret? {
    return try {
      val decrypted = cryptVault.decrypt(source.data)

      val decoder = BasicBSONDecoder()
      val bsonCallback: BSONCallback = BasicDBObjectCallback()
      decoder.decode(decrypted, bsonCallback)
      val deserialized = bsonCallback.get() as BSONObject
      val obj = deserialized.get("") as BasicDBObject
      val keyId = obj.get("keyId") as String
      val secret = obj.get("secret") as String

      Secret(keyId, secret)
    } catch (ex: Exception) {
      log.warn("Unable to read Secret from Binary!, returning an EMPTY ( invalid ) Secret to be used")
      Secret("", "")
    }
  }

  //TODO: refactor
  fun encode(secret: Secret): Binary {
    val en = BasicBSONEncoder()

    val document = Document.parse(secret.json())
    val serialized: ByteArray = en.encode(BasicBSONObject("", document))
    return Binary(cryptVault.encrypt(serialized))
  }

  fun convert(callbackSecurity: CallbackSecurity): BasicDBObject {
    val dbObject = BasicDBObject("method", callbackSecurity.method)
    dbObject["secret"] = encode(callbackSecurity.secret)
    return dbObject
  }

  fun convert(callback: Callback): Any {
    return if(callback.security == null) {
      callback.details()
    } else {
      val dbObject = Document.parse(callback.details().json())
      dbObject["security"] = convert(callback.security)

      return dbObject
    }
  }

  class BasicDBObjectCallback : BasicBSONCallback() {
    override fun create(): BSONObject {
      return BasicDBObject()
    }

    override fun createList(): BSONObject {
      return BasicDBList()
    }

    override fun createBSONCallback(): BSONCallback {
      return BasicDBObjectCallback()
    }
  }

}