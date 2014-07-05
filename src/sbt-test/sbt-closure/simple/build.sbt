import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.pipeline.Pipeline

organization := "net.ground5hark.sbt"

name := "sbt-closure-test"

version := "1.0.0"

scalaVersion := "2.10.4"

lazy val root = (project in file(".")).enablePlugins(SbtWeb)

// CoffeeScript should compile before anything in the asset pipeline
CoffeeScriptKeys.sourceMap := false

CoffeeScriptKeys.bare := true

// Pipeline which compiles assets, this one wraps code in a closure
val wrapPipelineTask = taskKey[Pipeline.Stage]("Wraps JS code in an anonymous function block")

includeFilter in wrapPipelineTask := new SimpleFileFilter(_.getName.endsWith(".js"))

wrapPipelineTask := { mappings =>
  val targetDir = (public in Assets).value
  def passThrough(f: File, name: String, targetDir: File) = {
    val outputFile = targetDir / name
    IO.copyFile(f, outputFile)
    (outputFile, name)
  }
  mappings.filter(m => (includeFilter in wrapPipelineTask).value.accept(m._1)).flatMap {
    case (f, name) =>
      Seq(passThrough(f, name, targetDir)) ++
        (if (name.endsWith("wrap.js")) {
          val outputName = name.replace(".js", ".wrapped.js")
          val outputFile = targetDir / outputName
          IO.write(outputFile, s"(function(){${IO.read(f)}}())")
          Seq((outputFile, outputName))
        } else {
          Seq.empty[(File, String)]
        })
  }
}

pipelineStages := Seq(wrapPipelineTask, closure)

val verifyMinified = taskKey[Unit]("Verify that the minified files are in fact minified")

verifyMinified := {
  var notMinified = false
  var notMinifiedName = ""
  (((public in Assets).value / "") ** "*.min.js").get.takeWhile(f => !notMinified).foreach { minFile: File =>
    val minifiedContents = IO.read(minFile)
    val unminifiedContents = IO.read(file(minFile.getAbsolutePath.replace(".min", "")))
    notMinifiedName = minFile.getAbsolutePath
    notMinified = minifiedContents.size >= notMinifiedName.size
  }
  if (notMinified)
    sys.error(s"File was not minified properly: $notMinifiedName")
}
