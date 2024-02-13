package ru.vkpay.sse.api

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect.Async
import cats.effect.kernel.Outcome
import cats.effect.syntax.all._
import org.http4s.{internal, Headers, HttpApp, HttpRoutes, Message, Request}
import org.typelevel.ci.CIString
import ru.vkpay.sse.api.Tracing.TokenRegex
import tofu.WithLocal
import tofu.generate.GenUUID
import tofu.logging.Logging
import tofu.syntax.context._
import tofu.syntax.funk.funKFrom
import tofu.syntax.logging._
import tofu.syntax.monadic._

import scala.util.matching.Regex

final case class Tracing(headerId: String) {
  def traceIdFunK[F[_]: Monad: GenUUID: WithLocal[*[_], C], C](req: Request[F], coerceFunc: String => C) =
    req.headers
      .get(CIString(headerId))
      .map(_.head.value.pure)
      .getOrElse(GenUUID[F].randomUUID.map(_.toString))
      .map(strTraceId => funKFrom[F](_.local(_ => coerceFunc(strTraceId))))

  def httpRoutes[F[_]: Monad: GenUUID: WithLocal[*[_], C], C](
    coerceCtx: String => C
  )(routes: HttpRoutes[F]): HttpRoutes[F] =
    Kleisli { req =>
      for {
        trans        <- OptionT.liftF(traceIdFunK(req, coerceCtx))
        tracedRoutes <- OptionT(trans(routes.run(req).value))
      } yield tracedRoutes
    }

  def apply[F[_]: Async: GenUUID: WithLocal[*[_], C]: Logging, C](
    coerceCtx: String => C,
    logHeaders: Boolean,
    logBody: Boolean,
    redactHeadersWhen: CIString => Boolean = Headers.SensitiveHeaders.contains
  )(routes: HttpApp[F]): HttpApp[F] = {

    def log(m: Message[F]) =
      internal.Logger.logMessage[F](m)(logHeaders, logBody, redactHeadersWhen) { msg =>
        m match {
          case _: Request[F] =>
            trace"${TokenRegex.replaceAllIn(msg, m => s"${m.group(1)}<token>${m.group(3)}")}"
          case _             => info"$msg"
        }
      }

    Kleisli { req =>
      for {
        trans        <- traceIdFunK(req, coerceCtx)
        tracedRoutes <- trans(
                          log(req) *>
                          routes
                            .run(req)
                            .flatTap(log)
                            .guaranteeCase {
                              case Outcome.Errored(t)   => errorCause"service raised an error" (t)
                              case Outcome.Canceled()   => warn"service canceled response for request"
                              case Outcome.Succeeded(_) => Async[F].unit
                            }
                        )
      } yield tracedRoutes
    }
  }

}

object Tracing {
  val Cpg               = new Tracing("X-CPG-Trace-Id")
  val TokenRegex: Regex = """(tokenn=)([^&]+)(&|$)""".r
}
