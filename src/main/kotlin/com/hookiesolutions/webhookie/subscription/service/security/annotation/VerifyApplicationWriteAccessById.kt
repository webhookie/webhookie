package com.hookiesolutions.webhookie.subscription.service.security.annotation

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 29/1/21 17:37
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class VerifyApplicationWriteAccessById(
  val idPosition: Int = 0
)
