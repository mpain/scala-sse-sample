package ru.vkpay.sse.api

import cats.data.Kleisli
import ru.vkpay.sse.model.context.{ApiConfig, AppContext, TraceContext, TraceId}
import ru.vkpay.sse.model.context.TraceContext._
import cats.effect.syntax.resource._
import cats.effect.{Async, Resource, Sync}
import cats.instances.option._
import com.comcast.ip4s.Port
import com.comcast.ip4s.Host
import org.http4s.{Request, Response}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import ru.vkpay.sse.api.routes.ApiRoutes
import tofu.lift.IsoK
import tofu.logging.{Logging, Logs}
import tofu.syntax.monadic._
import tofu.syntax.context._

import scala.util.control.NoStackTrace

object HttpServer {
  case class HttpServerInitErr(msg: String) extends Throwable(msg) with NoStackTrace

  def createHttpServer[
    F[_]: Async: Logging: TraceContext.HasLocal
  ](
    config: AppContext
  ): Resource[F, Server] =
    for {
      httpApi      <- ApiRouter.make[F](config.api).toResource
      wrapped       = Tracing.Cpg[F, TraceId](TraceId.apply, logHeaders = true, logBody = true)(httpApi)
      (host, port) <- Sync[F]
                        .fromOption(
                          (Host.fromString(config.http.host), Port.fromInt(config.http.port)).tupled,
                          HttpServerInitErr("Cannot parse http host or port")
                        )
                        .toResource
      server       <- EmberServerBuilder
                        .default[F]
                        .withHost(host)
                        .withPort(port)
                        .withHttpApp(wrapped)
                        .build
    } yield server

  def make[
    I[_]: Sync: AppContext.Has,
    F[_]: Async: TraceContext.HasLocal
  ]()(implicit
    logs: Logs[I, F],
    isoK: IsoK[I, F]
  ): Resource[I, Server] =
    for {
      implicit0(log: Logging[F]) <- logs.byName(HttpServer.getClass.getTypeName).toResource
      config                     <- context[I].toResource
      server                     <- createHttpServer[F](config).mapK(isoK.fromF)
    } yield server
}

object ApiRouter {
  object RouteBases {
    private val ApiVersion = "1.0"
    val ApiPrefix          = "sse"

    val Routes = s"/api/$ApiVersion"
  }

  def make[F[_]
    : Async: Logging: TraceContext.HasLocal](
    apiConfig: ApiConfig
  ): F[Kleisli[F, Request[F], Response[F]]] =
    for {
      routes <- ApiRoutes.make[F](apiConfig)
    } yield Router(
      RouteBases.ApiPrefix -> Router(
        s"${RouteBases.Routes}" -> Router(
          s"/sse" -> routes.sse
        )
      )
    ).orNotFound
}
