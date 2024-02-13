package ru.vkpay.sse.api.routes

import cats.effect.{Async, Sync}
import io.circe.{Encoder, Printer}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes, ServerSentEvent}
import ru.vkpay.sse.model.context.{ApiConfig, TraceContext}
import tofu.logging.Logging
import tofu.syntax.monadic._
import fs2._

import scala.annotation.nowarn

trait ApiRoutes[F[_]] {
  def sse: HttpRoutes[F]
  def ping: HttpRoutes[F]
}

@nowarn("cat=unused")
object ApiRoutes {
  def make[
    F[_]: Async: Logging: TraceContext.HasLocal
  ](config: ApiConfig): F[ApiRoutes[F]] =
    new Live[F]().pure[F].widen

  private[routes] class Live[F[_]: Async]() extends Http4sDsl[F]
    with ApiRoutes[F] {

    private val responsePrinter: Printer = Printer.noSpacesSortKeys.copy(dropNullValues = true)

    implicit def commonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderWithPrinterOf[F, A](responsePrinter)

    def sse: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root / "start" =>
      Ok(Stream.eval[F, ServerSentEvent](Sync[F].delay(ServerSentEvent.empty)))
    }

    def ping: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root / "ping" =>
      Ok("Pong")
    }
  }
}
