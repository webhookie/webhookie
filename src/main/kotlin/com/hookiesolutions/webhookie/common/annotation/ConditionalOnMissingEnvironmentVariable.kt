package com.hookiesolutions.webhookie.common.annotation

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.util.StringUtils


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/5/21 12:30
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(ConditionalOnMissingEnvironmentVariable.OnPropertyNotEmptyCondition::class)
annotation class ConditionalOnMissingEnvironmentVariable(
  val value: String
) {
  class OnPropertyNotEmptyCondition: Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
      val attrs = metadata.getAnnotationAttributes(
        ConditionalOnMissingEnvironmentVariable::class.java.name
      )
      val propertyName = attrs!!["value"] as String
      val host = System.getenv(propertyName)
      return !StringUtils.hasLength(host)
    }

  }
}


