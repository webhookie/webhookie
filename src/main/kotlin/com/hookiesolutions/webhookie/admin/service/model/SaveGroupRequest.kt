package com.hookiesolutions.webhookie.admin.service.model

import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.common.extension.isSimilarTo
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
  fun isSimilarToDefault(): Boolean {
    val consumerGroup = ConsumerGroup.DEFAULT
    return name.isSimilarTo(consumerGroup.name) ||
        iamGroupName.isSimilarTo(consumerGroup.iamGroupName)
  }
}
