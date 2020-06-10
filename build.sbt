/*
 * Copyright (c) 2012-2018 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

credentials in Global += Credentials(Path.userHome / ".ivy2" / ".credentials")

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaUnidocPlugin, GhpagesPlugin)
  .settings(
    organization := "com.snowplowanalytics",
    name := "scala-maxmind-iplookups",
    version := "0.5.3",
    description := "Scala wrapper for MaxMind GeoIP2 library - Sonic release",
    scalaVersion := "2.11.12",
    crossScalaVersions := Seq("2.11.12", "2.12.5"),
    scalacOptions := BuildSettings.compilerOptions,
    javacOptions := BuildSettings.javaCompilerOptions,
    scalafmtOnCompile := true
  )
  .settings(Seq(publishTo := Some(
    "Supersonic Artifactory" at "http://artifactory.rtb.ec2ssa.info:8081/artifactory/ext-release-local;build.timestamp=" + new java.util.Date().getTime)))
  .settings(BuildSettings.docSettings)
  .settings(BuildSettings.coverageSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.maxmind,
      Dependencies.catsEffect,
      Dependencies.cats,
      Dependencies.lruMap,
      Dependencies.scalaCheck,
      Dependencies.specs2
    )
  )
