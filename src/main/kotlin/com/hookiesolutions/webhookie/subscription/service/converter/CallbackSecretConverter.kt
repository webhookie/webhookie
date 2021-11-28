/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.subscription.service.converter

import com.bol.crypt.CryptVault
import com.hookiesolutions.webhookie.subscription.domain.callback.Callback
import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme.Companion.PROPERTY_KEY
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacDetails
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacDetails.Companion.PROPERTY_KEY_ID
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacDetails.Companion.PROPERTY_SECRET
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacSecurityScheme
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import org.bson.*
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
): Converter<Binary, HmacDetails> {
  override fun convert(source: Binary): HmacDetails {
    return try {
      val decrypted = cryptVault.decrypt(source.data)

      val decoder = BasicBSONDecoder()
      val bsonCallback: BSONCallback = BasicDBObjectCallback()
      decoder.decode(decrypted, bsonCallback)
      val deserialized = bsonCallback.get() as BSONObject
      val obj = deserialized.get("") as BasicDBObject
      val keyId = obj.get(PROPERTY_KEY_ID) as String
      val secret = obj.get(PROPERTY_SECRET) as String

      HmacDetails(keyId, secret)
    } catch (ex: Exception) {
      log.warn("Unable to read Secret from Binary!, returning an EMPTY ( invalid ) Secret to be used")
      HmacDetails("", "")
    }
  }

  //TODO: refactor
  fun encode(secret: HmacDetails): Binary {
    val en = BasicBSONEncoder()

    val document = Document.parse(secret.json())
    val serialized: ByteArray = en.encode(BasicBSONObject("", document))
    return Binary(cryptVault.encrypt(serialized))
  }

  fun convert(securityScheme: HmacSecurityScheme): BasicDBObject {
    val dbObject = BasicDBObject(PROPERTY_KEY, securityScheme.method.name)
    dbObject[PROPERTY_SECRET] = encode(securityScheme.details)
    return dbObject
  }

  fun convert(callback: Callback): Any {
    if(callback.securityScheme == null) {
      return callback.details()
    }

    if(callback.securityScheme.isHmac()) {
      val dbObject = Document.parse(callback.details().json())
      dbObject["securityScheme"] = convert(callback.securityScheme as HmacSecurityScheme)

      return dbObject
    }

    TODO("Implement OAuth2 here!")
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
