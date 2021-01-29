package com.hookiesolutions.webhookie.common.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 23:59
 */
@Component
public class IRIValidator implements ConstraintValidator<IRI, String> {
  @Override
  public void initialize(IRI constraintAnnotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if(!StringUtils.hasText(value)) {
      return true;
    }

    try {
      new URL(value);
      return true;
    } catch (MalformedURLException e) {
      return false;
    }
  }
}
