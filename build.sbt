name := "Scala Noggin"

scalaVersion := "2.9.1"

seq(webSettings: _*)

resolvers ++= Seq(
  "Scala Tools Releases" at "http://scala-tools.org/repo-releases/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Java.net" at "http://download.java.net/maven/2/"
)

libraryDependencies ++= {
  val liftVersion = "2.4"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-testkit" % liftVersion % "test->default")
}

libraryDependencies ++= Seq(
  "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
  "se.scalablesolutions.akka" % "akka-actor" % "1.2",
  "org.scala-tools.time" %% "time" % "0.5",
  "javax.transaction" % "jta" % "1.1",
  "net.sf.ehcache" % "ehcache-core" % "2.5.1",
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default",
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "junit" % "junit" % "4.8" % "test->default",
  "org.mockito" % "mockito-all" % "1.8.5" % "test->default",
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test->default",
  "se.scalablesolutions.akka" % "akka-testkit" % "1.2" % "test->default",
  "org.mortbay.jetty" % "jetty" % "6.1.26" % "test,container"
)