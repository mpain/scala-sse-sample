import sbt.*

object dependencies {

  object versions {
    val Skunk        = "0.5.1"
    val Tofu         = "0.12.0.1"
    val TofuGlass    = "0.2.1"
    val Scopt        = "4.1.0"
    val PureConfig   = "0.17.2"
    val Derevo       = "0.13.0"
    val Http4s       = "0.23.18"
    val Enumeratum   = "1.7.2"
    val Bouncycastle = "1.76"
    val Mouse        = "1.2.1"
    val Sttp         = "3.8.13"
    val CirceRefined = "0.14.5"
    val Bucket4j     = "8.2.RC2"
    val CatsRetry    = "3.1.0"

    val Jose4j = "0.9.3"

    val BetterMonadic4 = "0.3.1"
    val KindProjector  = "0.13.2"

    val ScalaTest = "3.2.15"
  }

  lazy val retry: Seq[ModuleID] = List(
    "com.github.cb372" %% "cats-retry" % versions.CatsRetry
  )

  lazy val mouse: Seq[ModuleID] = List(
    "org.typelevel" %% "mouse" % versions.Mouse
  )

  lazy val http4s: Seq[ModuleID] = List(
    "org.http4s" %% "http4s-dsl"          % versions.Http4s,
    "org.http4s" %% "http4s-ember-server" % versions.Http4s,
    "org.http4s" %% "http4s-circe"        % versions.Http4s,
    "io.circe"   %% "circe-refined"       % versions.CirceRefined
  )

  lazy val enumeratum: Seq[ModuleID] = List(
    "com.beachape" %% "enumeratum"       % versions.Enumeratum,
    "com.beachape" %% "enumeratum-circe" % versions.Enumeratum
  )

  lazy val bouncycastle: Seq[ModuleID] = List(
    "org.bouncycastle" % "bcprov-jdk15to18" % versions.Bouncycastle,
    "org.bouncycastle" % "bcpkix-jdk15to18" % versions.Bouncycastle
  )

  lazy val config: Seq[ModuleID] = List(
    "com.github.scopt"      %% "scopt"                  % versions.Scopt,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % versions.PureConfig
  )

  lazy val sttp: Seq[ModuleID] = List(
    "com.softwaremill.sttp.client3" %% "core"                 % versions.Sttp,
    "com.softwaremill.sttp.client3" %% "circe"                % versions.Sttp,
    "com.softwaremill.sttp.client3" %% "armeria-backend-cats" % versions.Sttp,
    "com.softwaremill.sttp.client3" %% "slf4j-backend"        % versions.Sttp
  )

  lazy val tofu: Seq[ModuleID] = List(
    "tf.tofu" %% "tofu-core-ce3"       % versions.Tofu,
    "tf.tofu" %% "tofu-derivation"     % versions.Tofu,
    "tf.tofu" %% "tofu-logging"        % versions.Tofu,
    "tf.tofu" %% "tofu-logging-layout" % versions.Tofu,
    "tf.tofu" %% "glass-core"          % versions.TofuGlass,
    "tf.tofu" %% "glass-macro"         % versions.TofuGlass
  )

  lazy val derevo: Seq[ModuleID] = List(
    "tf.tofu" %% "derevo-circe-magnolia" % versions.Derevo,
    "tf.tofu" %% "derevo-pureconfig"     % versions.Derevo
  )

  lazy val testing: Seq[ModuleID]   = List(
    "org.scalatest" %% "scalatest" % versions.ScalaTest
  ).map(_ % Test)

  lazy val jose4j: Seq[ModuleID] = List(
    "org.bitbucket.b_c" % "jose4j" % versions.Jose4j
  )

  lazy val skunk: Seq[ModuleID] = List(
    "org.tpolecat" %% "skunk-core"  % versions.Skunk,
    "org.tpolecat" %% "skunk-circe" % versions.Skunk
  )

  lazy val projectLibs: Seq[ModuleID] =
    tofu ++
    derevo ++
    http4s ++
    enumeratum ++
    bouncycastle ++
    config ++
    mouse ++
    skunk ++
    sttp ++
    jose4j ++
    retry ++
    testing

  lazy val compilerPlugins: List[ModuleID] =
    List(
      compilerPlugin("org.typelevel" %% "kind-projector"     % versions.KindProjector cross CrossVersion.full),
      compilerPlugin("com.olegpy"    %% "better-monadic-for" % versions.BetterMonadic4)
    )
}

