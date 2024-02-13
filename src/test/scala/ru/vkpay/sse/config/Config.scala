package ru.vkpay.sse.config

import cats.effect.Sync
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource
import ru.vkpay.sse.model.context.AppContext

object Config {
  def load[F[_]: Sync](
    pathOpt: Option[String]
  ): F[AppContext] =
    pathOpt
      .fold(ConfigSource.default)(path => ConfigSource.file(path).withFallback(ConfigSource.default))
      .loadF[F, AppContext]()
}
