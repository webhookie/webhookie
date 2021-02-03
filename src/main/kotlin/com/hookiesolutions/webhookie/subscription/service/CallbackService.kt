package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.UpdatableEntity.Companion.updatable
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.Callback
import com.hookiesolutions.webhookie.subscription.domain.CallbackRepository
import com.hookiesolutions.webhookie.subscription.service.model.CallbackRequest
import org.slf4j.Logger
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
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
  private val applicationRepository: ApplicationRepository,
  private val log: Logger
) {
  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  fun createCallback(applicationId: String, request: CallbackRequest): Mono<Callback> {
    log.info("adding Callback: '{}' to application: '{}'", request.requestTarget(), applicationId)
    return fetchApplicationWithWriteAccess(applicationId)
      .map { request.callback(applicationId) }
      .flatMap { repository.save(it)}
      .doOnNext { log.info("Callback '{}' was created successfully", it.details.requestTarget()) }
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  fun applicationCallbacks(applicationId: String): Flux<Callback> {
    log.info("fetching all application callbacks : '{}'", applicationId)
    return fetchApplicationWithReadAccess(applicationId)
      .flatMapMany { repository.findApplicationCallbacks(applicationId) }
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  fun applicationCallbackById(applicationId: String, id: String): Mono<Callback> {
    log.info("fetching callback : '{}'", id)
    return fetchApplicationWithReadAccess(applicationId)
      .flatMap { repository.findById(id) }
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  fun deleteApplicationCallbackById(applicationId: String, id: String): Mono<String> {
    log.info("deleting callback : '{}'", id)
    return fetchApplicationWithWriteAccess(applicationId)
      .flatMap { repository.findById(id) }
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("hasAuthority('${ROLE_CONSUMER}')")
  fun updateCallback(applicationId: String, id: String, body: CallbackRequest): Mono<Callback> {
    log.info("updating Callback: '{}', '{}'", body.requestTarget(), id)
    return fetchApplicationWithWriteAccess(applicationId)
      .flatMap { repository.findById(id) }
      .map { body.copy(it, applicationId) }
      .map { updatable(it) }
      .flatMap { repository.update(it, id) }
  }

  private fun fetchApplicationWithReadAccess(applicationId: String): Mono<Application> {
    return applicationRepository.findByIdVerifyingReadAccess(applicationId)
      .doOnError(AccessDeniedException::class.java) {
        log.warn("Current user doesn't have access to the application: '{}'", applicationId)
      }
  }

  private fun fetchApplicationWithWriteAccess(applicationId: String): Mono<Application> {
    return applicationRepository.findByIdVerifyingWriteAccess(applicationId)
      .doOnError(AccessDeniedException::class.java) {
        log.warn("Current user doesn't have access to the application: '{}'", applicationId)
      }
  }
}