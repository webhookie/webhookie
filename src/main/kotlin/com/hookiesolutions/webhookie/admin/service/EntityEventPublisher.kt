package com.hookiesolutions.webhookie.admin.service

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
  private val groupHasBeenDeletedChannel: MessageChannel,
  private val groupHasBeenUpdatedChannel: MessageChannel
) {
  fun <T> publishDeleteEvent(payload: EntityDeletedMessage<T>) {
    val message = MessageBuilder.withPayload(payload).build()
    groupHasBeenDeletedChannel.send(message)
  }

  fun <T> publishUpdateEvent(payload: EntityUpdatedMessage<T>) {
    val message = MessageBuilder.withPayload(payload).build()
    groupHasBeenUpdatedChannel.send(message)
  }
}