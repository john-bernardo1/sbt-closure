package net.ground5hark.sbt.closure

import com.typesafe.sbt.web.{PathMapping, SbtWeb}
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt._
import sbt.Keys._

object Import {
  val closure = TaskKey[Pipeline.Stage]("closure", "Runs JavaScript web assets through the Google closure compiler")

  object Closure {
    val flags = SettingKey[Seq[String]]("closure-flags", "Command line flags to pass to the closure compiler")
    val suffix = SettingKey[String]("closure-suffix", "Suffix to append to compiled files, default: \".min.js\"")
  }
}

class UncompiledJsFileFilter(suffix: String) extends FileFilter {
  override def accept(file: File): Boolean =
    // visible
    !HiddenFileFilter.accept(file) &&
    // not already compiled
    !file.getName.endsWith(suffix) &&
    // a JS file
    file.getName.endsWith(".js")
}

object SbtClosure extends AutoPlugin {
  override def requires = SbtWeb

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport._
  import Closure._

  override def projectSettings = Seq(
    flags := Seq.empty[String],
    suffix := ".min.js",
    includeFilter in closure := new UncompiledJsFileFilter(suffix.value),
    closure := closureCompile.value
  )

  private def closureCompile: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings: Seq[PathMapping] =>
      // TODO - Call closure compiler using CommandLineRunner(args).run()
      mappings.filter(m => (includeFilter in closure).value.accept(m._1))
  }
}