package com.hookiesolutions.webhookie.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 23:44
 */
@Documented
@Constraint(validatedBy = { IRIValidator.class} )
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface IRI {
  @SuppressWarnings("unused")
  String message() default "Invalid IRI";

  @SuppressWarnings("unused")
  Class<?>[] groups() default {};

  @SuppressWarnings("unused")
  Class<? extends Payload>[] payload() default {};
}
