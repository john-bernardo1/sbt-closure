package net.ground5hark.sbt.closure

import com.google.javascript.jscomp.CommandLineRunner
import com.typesafe.sbt.web.{PathMapping, SbtWeb}
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt._
import sbt.Keys._

object Import {
  val closure = TaskKey[Pipeline.Stage]("closure", "Runs JavaScript web assets through the Google closure compiler")

  object Closure {
    val flags = SettingKey[Seq[String]]("closure-flags", "Command line flags to pass to the closure compiler, example: Seq(\"--formatting=PRETTY_PRINT\", \"--accept_const_keyword\")")
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

private class SbtClosureCommandLineRunner(args: Array[String]) extends CommandLineRunner(args) {
  def compile(): Unit = doRun()
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

  object util {
    def withoutExt(name: String): String = name.substring(0, name.lastIndexOf("."))
  }

  private def invokeCompiler(src: File, target: File): Unit = {
    val compiler = new SbtClosureCommandLineRunner(Seq(s"--js=${src.getAbsolutePath}", s"--js_output_file=${target.getAbsolutePath}").toArray)
    if (compiler.shouldRunCompiler())
      compiler.compile()
    else
      sys.error("Invalid closure compiler configuration, check flags")
  }

  private def closureCompile: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings: Seq[PathMapping] =>
      val targetDir = (public in Assets).value
      mappings.filter(m => (includeFilter in closure).value.accept(m._1)).map {
        case (f, name) =>
          val outputFileName = s"${util.withoutExt(name)}${suffix.value}"
          val outputFile = targetDir / outputFileName
          invokeCompiler(f, outputFile)
          (outputFile, outputFileName)
        case u => sys.error(s"Unknown mapping: $u")
      }
  }
}