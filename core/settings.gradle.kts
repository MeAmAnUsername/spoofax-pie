rootProject.name = "spoofax3.core.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

include("spoofax.depconstraints")

include("aterm.common")
include("completions.common")
include("jsglr.common")
include("jsglr.pie")
include("jsglr1.common")
include("jsglr2.common")
include("esv.common")
include("stratego.common")
include("stratego.pie")
include("constraint.common")
include("constraint.pie")
include("nabl2.common")
include("statix.common")
include("statix.multilang")
include("statix.multilang.eclipse")
include("spt.api")
include("spoofax2.common")
include("tooling.eclipsebundle")

include("spoofax.core")
include("spoofax.test")

include("spoofax.cli")
include("spoofax.intellij")
include("spoofax.eclipse")

include("spoofax.compiler")
include("spoofax.compiler.dagger")
include("spoofax.compiler.spoofax2")
include("spoofax.compiler.spoofax2.dagger")
include("spoofax.compiler.interfaces")
include("spoofax.compiler.gradle")
include("spoofax.compiler.gradle.spoofax2")
include("spoofax.compiler.eclipsebundle")
