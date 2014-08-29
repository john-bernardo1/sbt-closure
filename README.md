sbt-closure
===========
[![Build Status](https://api.travis-ci.org/ground5hark/sbt-closure.png?branch=master)](https://travis-ci.org/ground5hark/sbt-closure)

[sbt-web] plugin which integrates with [Google’s Closure Compiler].

Plugin
======
Add the plugin to your `project/plugins.sbt`:
```scala
addSbtPlugin("net.ground5hark.sbt" % "sbt-closure" % "0.1.3")
```

Add the [Sonatype releases] resolver:
```scala
resolvers += Resolver.sonatypeRepo("releases")
```

Enable the [sbt-web] plugin for your project:
```scala
lazy val root = (project in file(".")).enablePlugins(SbtWeb)
```

Add the `closure` task to your asset pipeline in your `build.sbt`:
```scala
pipelineStages := Seq(closure)
```

Configuration options
=====================
Option              | Description
--------------------|------------
suffix              | Suffix to append to each file compiled by closure. Defaults to `".min.js"`
flags               | List of command line flags to provide to the closure compiler. Must be in the format of `--option-name=value` or `--option-flag`
parentDir           | Parent directory name where closure compiled JS will go. Defaults to `"closure-compiler"`

An example of providing options is below:

```scala
Closure.suffix := ".min.js"

Closure.flags := Seq("--formatting=PRETTY_PRINT", "--accept_const_keyword")
```

This will produce assets with the specified `Closure.suffix` suffix value under the `Closure.parentDir` directory within
the `target` folder. This will be `target/web/public/main` or `target/web/stage`. For a full list of closure compiler
options, see the [official documentation page]. You can also specify `includeFilter in closure` or
`excludeFilter in closure` to select files on a custom criteria.

License
=======
This code is licensed under the [MIT License].

[sbt-web]:https://github.com/sbt/sbt-web
[official documentation page]:https://developers.google.com/closure/compiler/docs/gettingstarted_app
[Google’s Closure Compiler]:https://developers.google.com/closure/compiler/
[MIT License]:http://opensource.org/licenses/MIT
[Sonatype releases]:https://oss.sonatype.org/content/repositories/releases/
