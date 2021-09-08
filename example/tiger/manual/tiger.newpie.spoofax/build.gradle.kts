plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.devenv.spoofax.gradle.project")
//  id("org.metaborg.spoofax.gradle.project")
}

spoofaxProject {
  inputIncludePatterns.add("*.pie")
  outputIncludePatterns.add("*.java")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  // Platforms
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))
  testAnnotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  // Main
  api(project(":tiger"))
  api(compositeBuild("spoofax.core"))
  api(compositeBuild("aterm.common"))
  api(compositeBuild("jsglr.pie"))
  api(compositeBuild("constraint.pie"))
  api(compositeBuild("spt.api"))
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.dagger")
  api("com.google.dagger:dagger")

  compileLanguage("org.metaborg:pie.lang:0.16.6")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")

  // Test
  testImplementation(compositeBuild("spoofax.test"))
  testCompileOnly("org.checkerframework:checker-qual-android")
}

sourceSets {
  main {
    java {
      srcDir("build/generated/sources/pie/")
    }
  }
}

afterEvaluate {
  tasks.named("spoofaxBuild") {
    outputs.upToDateWhen { false }
  }
}
