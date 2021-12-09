package com.hookiesolutions.webhookie.subscription.service.model

import com.hookiesolutions.webhookie.subscription.domain.callback.Callback

interface CreateCallbackRequest {
  fun callback(applicationId: String): Callback
  fun requestTarget(): String
}
