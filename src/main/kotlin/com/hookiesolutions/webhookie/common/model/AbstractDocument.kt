package com.hookiesolutions.webhookie.common.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.format.annotation.DateTimeFormat
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2019-01-20 15:42
 */
abstract class AbstractDocument {
  @CreatedDate
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  @JsonIgnore
  var createdDate: Instant? = null

  @CreatedBy
  @JsonIgnore
  var createdBy: String? = null

  @LastModifiedDate
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  @JsonIgnore
  var lastModifiedDate: Instant? = null

  @LastModifiedBy
  @JsonIgnore
  var lastModifiedBy: String? = null

  @Version
  @JsonIgnore
  var version: Long? = null

  class Keys {
    companion object {
      const val KEY_VERSION = "version"
    }
  }
}
