package com.hookiesolutions.webhookie.subscription.service.model

import com.hookiesolutions.webhookie.common.validation.Url
import com.hookiesolutions.webhookie.subscription.domain.Callback
import com.hookiesolutions.webhookie.subscription.domain.CallbackSecurity
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
    val result = Callback(name, applicationId, httpMethod, url, security)
    result.version = entity.version
    result.id = entity.id
    result.createdDate = entity.createdDate
    result.createdBy = entity.createdBy
    return result
  }
}
