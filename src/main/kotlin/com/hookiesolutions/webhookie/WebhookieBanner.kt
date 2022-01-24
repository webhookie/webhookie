/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie

import org.springframework.boot.Banner
import org.springframework.boot.SpringBootVersion
import org.springframework.boot.ansi.AnsiColor
import org.springframework.boot.ansi.AnsiOutput
import org.springframework.boot.ansi.AnsiStyle
import org.springframework.core.env.Environment
import java.io.PrintStream
import java.lang.StringBuilder

class WebhookieBanner: Banner {
  override fun printBanner(environment: Environment?, sourceClass: Class<*>, out: PrintStream) {
    for (line in BANNER) {
      out.println(line)
    }
    var version = SpringBootVersion.getVersion()
    version = if (version != null) " (v$version)" else ""
    val bootPadding = StringBuilder()
    while (bootPadding.length < STRAP_LINE_SIZE - (version.length + SPRING_BOOT.length)) {
      bootPadding.append(" ")
    }

    val webhookiePadding = StringBuilder()
    while (webhookiePadding.length < STRAP_LINE_SIZE - (VERSION.length + WEBHOOKIE.length)) {
      webhookiePadding.append(" ")
    }

    out.println(
      AnsiOutput.toString(
        AnsiColor.GREEN,
        WEBHOOKIE,
        AnsiColor.DEFAULT,
        webhookiePadding.toString(),
        AnsiStyle.FAINT,
        VERSION,
      )
    )
    out.println(
      AnsiOutput.toString(
        AnsiColor.YELLOW,
        SPRING_BOOT,
        AnsiColor.DEFAULT,
        bootPadding.toString(),
        AnsiStyle.FAINT,
        version
      )
    )
    out.println()
  }

  companion object {
    private const val SPRING_BOOT = " :: Spring Boot :: "
    private const val WEBHOOKIE = " :: Webhookie :: "
    private const val VERSION = "(v1.2.0)"
    private const val STRAP_LINE_SIZE = 51
    private val BANNER = arrayOf(
      "",
      "               _     _                 _    _",
      "              | |   | |               | |  (_)",
      " __      _____| |__ | |__   ___   ___ | | ___  ___",
      " \\ \\ /\\ / / _ \\ '_ \\| '_ \\ / _ \\ / _ \\| |/ / |/ _ \\",
      "  \\ V  V /  __/ |_) | | | | (_) | (_) |   <| |  __/",
      "   \\_/\\_/ \\___|_.__/|_| |_|\\___/ \\___/|_|\\_\\_|\\___|",
    )
  }
}
