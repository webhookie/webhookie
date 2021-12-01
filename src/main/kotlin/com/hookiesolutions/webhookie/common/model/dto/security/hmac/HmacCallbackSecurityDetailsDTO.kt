package com.hookiesolutions.webhookie.common.model.dto.security.hmac

import com.hookiesolutions.webhookie.common.model.dto.security.CallbackSecurityDetailsDTO

data class HmacCallbackSecurityDetailsDTO(
  val keyId: String
): CallbackSecurityDetailsDTO
