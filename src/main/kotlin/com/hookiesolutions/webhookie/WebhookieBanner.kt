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
    private const val VERSION = "(v1.0.0)"
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
