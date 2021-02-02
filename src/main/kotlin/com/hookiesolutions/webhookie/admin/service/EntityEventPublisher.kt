package com.hookiesolutions.webhookie.admin.service

import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.common.message.entity.EntityDeletedMessage
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 22/1/21 14:58
 */
@Service
class EntityEventPublisher(
  private val consumerGroupHasBeenDeletedChannel: MessageChannel,
  private val consumerGroupHasBeenUpdatedChannel: MessageChannel,
  private val providerGroupHasBeenDeletedChannel: MessageChannel,
  private val providerGroupHasBeenUpdatedChannel: MessageChannel
) {
  fun <P,T> publishDeleteEvent(payload: EntityDeletedMessage<P>, deletedEntity: T) {
    val message = MessageBuilder.withPayload(payload).build()
    if(deletedEntity is ConsumerGroup) {
      consumerGroupHasBeenDeletedChannel.send(message)
    } else {
      providerGroupHasBeenDeletedChannel.send(message)
    }
  }

  fun <P,T> publishUpdateEvent(payload: EntityUpdatedMessage<P>, updatedEntity: T) {
    if(payload.hasChanges()) {
      val message = MessageBuilder.withPayload(payload).build()
      if(updatedEntity is ConsumerGroup) {
        consumerGroupHasBeenUpdatedChannel.send(message)
      } else {
        providerGroupHasBeenUpdatedChannel.send(message)
      }
    }
  }
}