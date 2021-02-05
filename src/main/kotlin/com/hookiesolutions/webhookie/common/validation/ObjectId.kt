package com.hookiesolutions.webhookie.common.validation

import org.springframework.stereotype.Component
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 5/2/21 16:37
 */
@Constraint(validatedBy = [ObjectIdValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ObjectId(
  val message: String = "Invalid ObjectId",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

@Component
class ObjectIdValidator: ConstraintValidator<ObjectId, String> {
  override fun isValid(value: String, context: ConstraintValidatorContext): Boolean {
    return org.bson.types.ObjectId.isValid(value)
  }
}
