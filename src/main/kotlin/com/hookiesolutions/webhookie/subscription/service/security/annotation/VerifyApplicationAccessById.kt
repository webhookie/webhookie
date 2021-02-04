package com.hookiesolutions.webhookie.subscription.service.security.annotation

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/2/21 12:12
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class VerifyApplicationAccessById(
  val idPosition: Int = 0,
  val access: ApplicationAccessType
)
