package ru.vkpay.sse.model

import cats.syntax.option._
import derevo.derive
import derevo.pureconfig.pureconfigReader
import glass.macros.{promote, ClassyOptics}
import tofu.WithContext.ContextInstances2
import tofu.{WithContext, WithLocal}
import tofu.logging.derivation.loggable

import scala.concurrent.duration.FiniteDuration

object context {

  @derive(loggable, pureconfigReader)
  @ClassyOptics
  case class AppContext(
    @promote http: HttpConfig,
    @promote api: ApiConfig
  )

  object AppContext extends WithContext.Companion[AppContext]

  @derive(loggable, pureconfigReader)
  @ClassyOptics
  case class HttpConfig(host: String, port: Int)

  @derive(loggable, pureconfigReader)
  case class ApiConfig(
    needCache: Option[Boolean],
    requestTtl: Option[FiniteDuration],
    secret: String
  )

  @derive(loggable)
  case class TraceId(value: String) extends AnyVal

  object TraceId extends WithContext.Companion[TraceId] {
    type HasLocal[F[_]] = F WithLocal TraceId
  }

  @derive(loggable)
  @ClassyOptics
  final case class TraceContext(@promote traceId: TraceId, id: Option[String])

  object TraceContext extends WithContext.Companion[TraceContext] with ContextInstances2[TraceContext] {
    type HasLocal[F[_]] = F WithLocal TraceContext

    lazy val empty: TraceContext = TraceContext(TraceId("TRACE-ROOT"), none)
  }
}
