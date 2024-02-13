package ru.vkpay.sse

import cats.Parallel
import cats.data.ReaderT
import cats.effect.implicits.effectResourceOps
import cats.effect.std.Console
import ru.vkpay.sse.model.context.AppContext
import ru.vkpay.sse.model.context.TraceContext
import cats.effect.{Async, ExitCode, IO, IOApp, Resource, Sync}
import org.http4s.server.Server
import ru.vkpay.sse.api.HttpServer
import ru.vkpay.sse.config.{Args, Config}
import tofu.lift.IsoK
import tofu.logging.{LoggableContext, Logging, Logs}
import tofu.syntax.context.runContext
import tofu.{WithContext, WithRun}
import tofu.syntax.logging._
import tofu.syntax.monadic._

import scala.annotation.nowarn

object SseApp extends IOApp {
  type InitF[A] = IO[A]
  type RunF[A]  = ReaderT[InitF, TraceContext, A]

  override def run(args: List[String]): IO[ExitCode] =
    program[InitF, RunF](Args.read(args)).use(_ => IO.never as ExitCode.Success)

  def program[
    I[_]: Async,
    F[_]: Async: Parallel: Console
  ](args: Args)(implicit WR: WithRun[F, I, TraceContext]): Resource[I, Server] = {
    type WC[T[_]] = WithContext[T, AppContext]

    for {
      implicit0(logging: Logging[I]) <- Logs.sync[I, I].byName(SseApp.getClass.getSimpleName).toResource
      initCtx                        <- Config.load[I](args.configFilePathOpt).toResource
      implicit0(ctx: WC[I])          <- WithContext.const[I, AppContext](initCtx).pure[I].toResource
      result                         <- init[I, F]()
    } yield result
  }

  @nowarn("cat=unused")
  def init[
    I[_]: Sync: AppContext.Has: Logging,
    F[_]: Async: Parallel: Console
  ]()(implicit WR: WithRun[F, I, TraceContext]): Resource[I, Server] =
    for {
      _                                 <- info"Starting...".toResource
      implicit0(lc: LoggableContext[F]) <- LoggableContext.of[F].instance[TraceContext].pure[I].toResource
      implicit0(logs: Logs[I, F])       <- Logs.withContext[I, F].pure[I].toResource
      implicit0(isoK: IsoK[I, F])       <- runContext(WR.subIso)(TraceContext.empty).toResource
      server                            <- HttpServer.make[I, F]()
    } yield server

}
