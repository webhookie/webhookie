package com.hookiesolutions.webhookie.portal.service.model

import com.hookiesolutions.webhookie.portal.domain.ConsumerGroup
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
  fun consumerGroup(): ConsumerGroup {
    return ConsumerGroup(name, description, iamGroupName)
  }
}
