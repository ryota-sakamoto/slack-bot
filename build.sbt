import Dependencies._

lazy val root = (project in file(".")).
    settings(
        inThisBuild(List(
            scalaVersion := "2.12.4",
            version      := "1.0"
        )),
        name := "slack-bot",
        libraryDependencies ++= Seq(
            "com.github.gilbertw1" %% "slack-scala-client" % "0.2.2",
            "net.databinder.dispatch" %% "dispatch-core" % "0.13.2",
            "org.json4s" %% "json4s-native" % "3.6.0-M1",
            "net.ruippeixotog" %% "scala-scraper" % "2.0.0"
        )
    )
assemblyMergeStrategy in assembly := {
    case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
    case "application.conf"                            => MergeStrategy.concat
    case "unwanted.txt"                                => MergeStrategy.discard
    case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
}