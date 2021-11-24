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

package com.hookiesolutions.webhookie.subscription.service.model

import com.hookiesolutions.webhookie.common.validation.Url
import com.hookiesolutions.webhookie.subscription.domain.callback.Callback
import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurity
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacSecret
import org.springframework.http.HttpMethod
import javax.validation.constraints.NotBlank

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/2/21 01:41
 */
data class CallbackRequest(
  @field:NotBlank
  val name: String,
  val httpMethod: HttpMethod,
  @field:Url
  val url: String,
  val security: CallbackSecurity?
) {
  fun requestTarget(): String {
    return "${httpMethod.name} $url"
  }

  fun callback(applicationId: String): Callback {
    return Callback(
      name,
      applicationId,
      httpMethod,
      url,
      security
    )
  }

  //TODO: refactor this and use mongodb update instead
  fun copy(entity: Callback, applicationId: String): Callback {
    val result = Callback(name, applicationId, httpMethod, url, createCallbackSecurity(entity.security, security))
    result.version = entity.version
    result.id = entity.id
    result.createdDate = entity.createdDate
    result.createdBy = entity.createdBy
    return result
  }

  private fun createCallbackSecurity(currentSecurity: CallbackSecurity?, newSecurity: CallbackSecurity?): CallbackSecurity? {
    if(newSecurity == null) {
      return null
    }

    val secret = newSecurity.secret.secret
    if(currentSecurity == null) {
      return CallbackSecurity(secret = HmacSecret(newSecurity.secret.keyId, secret))
    }

    return if(secret.replace("*", "").trim().isNotBlank()) {
      CallbackSecurity(secret = HmacSecret(newSecurity.secret.keyId, secret))
    } else {
      CallbackSecurity(secret = HmacSecret(newSecurity.secret.keyId, currentSecurity.secret.secret))
    }
  }
}
