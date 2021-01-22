package com.hookiesolutions.webhookie.admin.service.model

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
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
) {
  fun accessGroup(clazz: Class<out AccessGroup>): AccessGroup {
    return if(clazz == ConsumerGroup::class.java) {
      ConsumerGroup(name, description, iamGroupName)
    } else {
      ProviderGroup(name, description, iamGroupName)
    }
  }
}