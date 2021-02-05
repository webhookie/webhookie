package com.hookiesolutions.webhookie.common.validation

import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.net.MalformedURLException
import java.net.URL
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 5/2/21 16:52
 */
@Constraint(validatedBy = [UrlValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Url(
  val message: String = "Invalid ObjectId",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

@Component
class UrlValidator: ConstraintValidator<Url, String> {
  override fun isValid(value: String, context: ConstraintValidatorContext): Boolean {
    return if (!StringUtils.hasText(value)) {
      true
    } else try {
      URL(value)
      true
    } catch (e: MalformedURLException) {
      false
    }
  }
}

