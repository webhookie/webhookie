package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.UpdatableEntity.Companion.updatable
import com.hookiesolutions.webhookie.subscription.domain.Callback
import com.hookiesolutions.webhookie.subscription.domain.CallbackRepository
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionRepository
import com.hookiesolutions.webhookie.subscription.service.converter.CallbackSecretConverter
import com.hookiesolutions.webhookie.subscription.service.model.CallbackRequest
import com.hookiesolutions.webhookie.subscription.service.security.annotation.ApplicationAccessType
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationAccessById
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/2/21 01:40
 */
@Service
class CallbackService(
  private val repository: CallbackRepository,
  private val converter: CallbackSecretConverter,
  private val subscriptionRepository: SubscriptionRepository,
  private val log: Logger,
) {
  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.WRITE)
  fun createCallback(applicationId: String, request: CallbackRequest): Mono<Callback> {
    log.info("adding Callback: '{}' to application: '{}'", request.requestTarget(), applicationId)
    val callback = request.callback(applicationId)
    return repository.save(callback)
      .doOnNext { log.info("Callback '{}' was created successfully", it.requestTarget()) }
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.READ)
  fun applicationCallbacks(applicationId: String): Flux<Callback> {
    log.info("fetching all application callbacks : '{}'", applicationId)
    return repository.findApplicationCallbacks(applicationId)
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.READ)
  fun applicationCallbackById(applicationId: String, id: String): Mono<Callback> {
    log.info("fetching callback : '{}'", id)
    return repository.findById(id)
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.WRITE)
  fun deleteApplicationCallbackById(applicationId: String, id: String): Mono<String> {
    log.info("deleting callback : '{}'", id)
    return repository.findById(id)
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  @VerifyApplicationAccessById(access = ApplicationAccessType.WRITE)
  @Transactional
  fun updateCallback(applicationId: String, id: String, body: CallbackRequest): Mono<Callback> {
    log.info("updating Callback: '{}', '{}'", body.requestTarget(), id)

    return repository.findById(id)
      .map { updatable(body.copy(it, applicationId)) }
      .flatMap { repository.update(it, id) }
      .zipWhen { callback ->
        //TODO: refactor
        log.info("Updating all Callback '{}' Subscriptions", callback.name)
        val details = converter.convert(callback)

        subscriptionRepository.updateCallbackSubscriptions(id, details)
      }
      .doOnNext { log.info("Callback '{}' was updated successfully", it.t1.requestTarget()) }
      .map { it.t1 }
  }
}