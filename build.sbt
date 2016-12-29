
organization := "megaannum"

name := "logging_logsf"

version := "1.0.0"

scalaVersion := "2.12.1"

autoScalaLibrary := true

// scalaHome := Some(file("/scratch/support/scala-2.11.8"))

publishTo := Some(Resolver.file("file", new File("/usr/local/jars/")))

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

// libraryDependencies += "org.scala-lang" % "scala-actors" % "2.11.4"

// see: http://www.scalatest.org/install

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"


