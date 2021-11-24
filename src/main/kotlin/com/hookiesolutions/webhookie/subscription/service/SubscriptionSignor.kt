/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.subscription.utils.CryptoUtils
import com.hookiesolutions.webhookie.subscription.utils.CryptoUtils.Companion.ALG
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionSignature
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Service
class SubscriptionSignor(
  private val log: Logger,
  private val timeMachine: TimeMachine
) {
  fun sign(subscription: Subscription, consumerMessage: ConsumerMessage, spanId: String): Mono<SubscriptionSignature> {
    log.info("{} Signing message with topic: '{}' for span '{}'", subscription.callback.securityScheme!!.method, consumerMessage.topic, spanId)
    val time = timeMachine.now().toString()
    return Mono.justOrEmpty(subscription.callback.securityScheme)
      .zipWhen { CryptoUtils.hmac(it.secret.secret, subscription, time, consumerMessage.traceId, spanId) }
      .map {
        SubscriptionSignature.Builder()
          .keyId(it.t1.secret.keyId)
          .algorithm(ALG)
          .traceId(consumerMessage.traceId)
          .spanId(spanId)
          .date(time)
          .signature(it.t2)
          .build()
      }
  }
}
