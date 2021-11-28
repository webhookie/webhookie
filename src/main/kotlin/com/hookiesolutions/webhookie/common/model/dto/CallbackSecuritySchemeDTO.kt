package com.hookiesolutions.webhookie.common.model.dto

abstract class CallbackSecuritySchemeDTO

data class HmacCallbackSecuritySchemeDTO(
  val keyId: String
): CallbackSecuritySchemeDTO()

data class OAuth2CallbackSecuritySchemeDTO(
  val tokenEndpoint: String,
  val clientId: String
): CallbackSecuritySchemeDTO()
