package net.ground5hark.sbt.closure

import com.google.javascript.jscomp.CommandLineRunner
import com.typesafe.sbt.web.{PathMapping, SbtWeb}
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt._
import sbt.Keys._

import scala.collection.mutable.ListBuffer

object Import {
  val closure = TaskKey[Pipeline.Stage]("closure", "Runs JavaScript web assets through the Google closure compiler")

  object Closure {
    val flags = SettingKey[Seq[String]]("closure-flags", "Command line flags to pass to the closure compiler, example: Seq(\"--formatting=PRETTY_PRINT\", \"--accept_const_keyword\")")
    val suffix = SettingKey[String]("closure-suffix", "Suffix to append to compiled files, default: \".min.js\"")
    val parentDir = SettingKey[String]("closure-parent-dir", "Parent directory name where closure compiled JS will go, default: \"closure-compiler\"")
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
  final val OutputDir = "closure-compiler"

  override def requires = SbtWeb

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport._
  import Closure._

  override def projectSettings = Seq(
    flags := ListBuffer.empty[String],
    suffix := ".min.js",
    parentDir := "closure-compiler",
    includeFilter in closure := new UncompiledJsFileFilter(suffix.value),
    closure := closureCompile.value
  )

  object util {
    def withoutExt(name: String): String = name.substring(0, name.lastIndexOf("."))
    def withParent(f: File): String = f.getParentFile.getName + "/" + f.getName
  }

  private def invokeCompiler(src: File, target: File, flags: Seq[String]): Unit = {
    val opts = Seq(s"--js=${src.getAbsolutePath}", s"--js_output_file=${target.getAbsolutePath}") ++
      flags.filterNot(s => s.trim.startsWith("--js=") || s.trim.startsWith("--js_output_file="))
    val compiler = new SbtClosureCommandLineRunner(opts.toArray)
    if (compiler.shouldRunCompiler())
      compiler.compile()
    else
      sys.error("Invalid closure compiler configuration, check flags")
  }

  private def closureCompile: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings: Seq[PathMapping] =>
      val targetDir = (public in Assets).value / parentDir.value
      val compileMappings = mappings.view.filter(m => (includeFilter in closure).value.accept(m._1)).toMap

      // Only do work on files which have been modified
      val runCompiler = FileFunction.cached(streams.value.cacheDirectory / parentDir.value, FilesInfo.hash) { files =>
        files.map { f =>
          val outputFileSubPath = s"${util.withoutExt(compileMappings(f))}${suffix.value}"
          val outputFile = targetDir / outputFileSubPath
          IO.createDirectory(outputFile.getParentFile)
          streams.value.log.info(s"Closure compiler executing on file: ${compileMappings(f)}")
          invokeCompiler(f, outputFile, flags.value)
          outputFile
        }
      }

      val compiled = runCompiler(compileMappings.keySet).map { outputFile =>
        (outputFile, util.withParent(outputFile))
      }.toSeq

      compiled ++ mappings.filter {
        // Handle duplicate mappings
        case (mappingFile, mappingName) =>
          val include = compiled.filter(_._2 == mappingName).isEmpty
          if (!include)
            streams.value.log.info(s"Closure compiler encountered a duplicate mapping for $mappingName and will " +
              "prefer the closure compiled version instead. If you want to avoid this, make sure you aren't " +
              "including minified and non-minified sibling assets in the pipeline.")
          include
      }
  }
}