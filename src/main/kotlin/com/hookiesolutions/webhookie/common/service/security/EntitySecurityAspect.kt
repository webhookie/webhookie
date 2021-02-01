package com.hookiesolutions.webhookie.common.service.security

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/2/21 16:21
 */
@Component
@Aspect
class EntitySecurityAspect {

  @Pointcut("@annotation(com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeDeleted)")
  fun annotatedVerifyEntityCanBeDeleted() {
  }

  @Pointcut("@annotation(com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeUpdated)")
  fun annotatedVerifyEntityCanBeUpdated() {
  }

  @Before("annotatedVerifyEntityCanBeDeleted() && args(deletableEntity)")
  fun checkDeleteAccess(jp: JoinPoint, deletableEntity: DeletableEntity<out AbstractEntity>) {
    if(!deletableEntity.deletable) {
      throw AccessDeniedException("Entity is not deletable!")
    }
  }

  @Before("annotatedVerifyEntityCanBeUpdated() && args(updatableEntity, id)")
  fun checkUpdateAccess(jp: JoinPoint, updatableEntity: UpdatableEntity<out AbstractEntity>, id: String) {
    if(!updatableEntity.updatable) {
      throw AccessDeniedException("Entity is not updatable!")
    }
  }
}