package ru.vkpay.sse.config

import cats.syntax.option._
import scopt.OParser

final case class Args(
  configFilePathOpt: Option[String]
)

object Args {
  val AppName    = "scala-sse-service"
  val AppVersion = "1.x"

  def read(args: List[String]): Args =
    OParser.parse(parser, args, empty).getOrElse(empty)

  lazy val empty: Args = Args(none)

  private val builder = OParser.builder[Args]

  private val parser = {
    import builder._
    OParser.sequence(
      programName(AppName),
      head(AppName, AppVersion),
      opt[String]('c', "config-path")
        .action((x, c) => c.copy(configFilePathOpt = x.some))
        .text("config-path is a string")
    )
  }
}
