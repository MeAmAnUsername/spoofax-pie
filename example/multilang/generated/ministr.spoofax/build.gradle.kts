import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

languageAdapterProject {
  languageProject.set(project(":ministr"))
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime()
    withMultilangAnalyzer().run {
      rootModule("mini-str/mini-str-typing")
      preAnalysisStrategy("pre-analyze")
      postAnalysisStrategy("post-analyze")
      contextId("mini-sdf-str")
      fileConstraint("mini-str/mini-str-typing!mstrProgramOK")
      projectConstraint("mini-str/mini-str-typing!mstrProjectOK")
    }
  }
}
