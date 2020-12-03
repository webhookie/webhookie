package com.hookiesolutions.webhookie.config.mongo

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.service.TimeMachine
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback
import org.springframework.stereotype.Component
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:33
 */
@Component
class EntityAuditorAware<T : AbstractEntity>(
  private val timeMachine: TimeMachine,
  private val log: Logger
) : ReactiveBeforeConvertCallback<T> {
  override fun onBeforeConvert(entity: T, collection: String): Publisher<T> {
    log.debug("adding audit data to the document: '{}'", entity.javaClass.name)
    entity.createdDate = timeMachine.now()
    return entity.toMono()
  }
}