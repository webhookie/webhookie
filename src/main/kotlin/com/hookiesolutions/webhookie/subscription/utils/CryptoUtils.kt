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

package com.hookiesolutions.webhookie.subscription.utils

import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
class CryptoUtils {
  companion object {
    const val ALG = "HmacSHA256"

    fun hmac(secret: String, subscription: Subscription, time: String, traceId: String, spanId: String): Mono<String> {
      return hmac(secret, subscription.callback, time, traceId, spanId)
    }

    fun hmac(secret: String, callback: CallbackDetails, time: String, traceId: String, spanId: String): Mono<String> {
      val signatureValue =
        "(request-target): ${callback.requestTarget()}" +
            " date: $time" +
            " x-trace-id: $traceId" +
            " x-span-id: $spanId"
      return hmac(signatureValue, secret)
    }

    fun hmac(value: String, key: String): Mono<String> {
      return try {
        val sha256Hmac = Mac.getInstance(ALG)
        val secretKey = SecretKeySpec(key.toByteArray(), ALG)
        sha256Hmac.init(secretKey)
        val sign = sha256Hmac.doFinal(value.toByteArray())

        Base64.getEncoder().encodeToString(sign)
          .toMono()
      } catch (e: Exception) {
        Mono.error(e)
      }
    }
  }
}
