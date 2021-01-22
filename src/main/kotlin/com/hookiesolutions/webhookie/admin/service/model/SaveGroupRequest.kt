package com.hookiesolutions.webhookie.admin.service.model

import javax.validation.constraints.NotBlank

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 18:34
 */
data class SaveGroupRequest(
  @field:NotBlank
  val name: String,
  @field:NotBlank
  val description: String,
  @field:NotBlank
  val iamGroupName: String
)