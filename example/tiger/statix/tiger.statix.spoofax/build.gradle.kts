import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*
import mb.spoofax.compiler.util.StringUtil.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  // Platforms
  configurations.forEach { add(it.name, platform(compositeBuild("spoofax.depconstraints"))) }

  //api(compositeBuild("statix.completions"))
}

languageAdapterProject {
  languageProject.set(project(":tiger.statix"))
  compilerInput {
    withParser().run {
      classKind(ClassKind.Manual)
    }
    withStyler().run {
      classKind(ClassKind.Manual)
    }
    withCompleter().run {
      classKind(ClassKind.Manual)
    }
    withStrategoRuntime().run {

    }
    withConstraintAnalyzer().run {
      classKind(ClassKind.Manual)
    }
    project.classKind(ClassKind.Manual)
    project.configureCompilerInput()
    project.additionalModules(listOf(TypeInfo.of("mb.tiger.statix.spoofax", "SpoofaxModule")))
  }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  val taskPackageId = "mb.tiger.statix.spoofax.task"
  val commandPackageId = "mb.tiger.statix.spoofax.command"
  val showArgsType = TypeInfo.of("mb.tiger.statix.spoofax.task", "TigerShowArgs")

  // Compile file
  val compileFile = TypeInfo.of(taskPackageId, "TigerCompileFile")
  addTaskDefs(compileFile)
  val compileFileCommand = CommandDefRepr.builder()
    .type(commandPackageId, "TigerCompileFileCommand")
    .taskDefType(compileFile)
    .argType(compileFile.appendToId(".Args"))
    .displayName("Compile file (list literals)")
    .description("")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
    .addAllParams(listOf(
      ParamRepr.of("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.context(CommandContextType.File))
    ))
    .build()
  addCommandDefs(compileFileCommand)

  // Compile file (Alt)
  val compileFileAlt = TypeInfo.of(taskPackageId, "TigerCompileFileAlt")
  addTaskDefs(compileFileAlt)
  val compileFileAltCommand = CommandDefRepr.builder()
    .type(commandPackageId, "TigerCompileFileAltCommand")
    .taskDefType(compileFileAlt)
    .argType(compileFileAlt.appendToId(".Args"))
    .displayName("Alternative compile file")
    .description("")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
    .addAllParams(listOf(
      ParamRepr.of("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.context(CommandContextType.File)),
      ParamRepr.of("listDefNames", TypeInfo.ofBoolean(), false, ArgProviderRepr.value("true")),
      ParamRepr.of("base64Encode", TypeInfo.ofBoolean(), false, ArgProviderRepr.value("false")),
      ParamRepr.of("compiledFileNameSuffix", TypeInfo.ofString(), false, ArgProviderRepr.value(doubleQuote("defnames.aterm")))
    ))
    .build()
  addCommandDefs(compileFileAltCommand)

  // Compile directory
  val compileDirectory = TypeInfo.of(taskPackageId, "TigerCompileDirectory")
  addTaskDefs(compileDirectory)
  val compileDirectoryCommand = CommandDefRepr.builder()
    .type(commandPackageId, "TigerCompileDirectoryCommand")
    .taskDefType(compileDirectory)
    .argType(compileDirectory.appendToId(".Args"))
    .displayName("Compile directory (list definition names)")
    .description("")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
    .addAllParams(listOf(
      ParamRepr.of("dir", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.context(CommandContextType.Directory))
    ))
  .build()
  addCommandDefs(compileDirectoryCommand)

  // Show parsed AST
  val showParsedAst = TypeInfo.of(taskPackageId, "TigerShowParsedAst")
  addTaskDefs(showParsedAst)
  val showParsedAstCommand = CommandDefRepr.builder()
    .type(commandPackageId, "TigerShowParsedAstCommand")
    .taskDefType(showParsedAst)
    .argType(showArgsType)
    .displayName("Show parsed AST")
    .description("Shows the parsed Abstract Syntax Tree of the program.")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.ResourceKey)),
      ParamRepr.of("region", TypeInfo.of("mb.common.region", "Region"), false, ArgProviderRepr.context(CommandContextType.Region))
    ))
    .build()
  addCommandDefs(showParsedAstCommand)

  // Show desugared AST
  val showDesugaredAst = TypeInfo.of(taskPackageId, "TigerShowDesugaredAst")
  addTaskDefs(showDesugaredAst)
  val showDesugaredAstCommand = CommandDefRepr.builder()
    .type(commandPackageId, "TigerShowDesugaredAstCommand")
    .taskDefType(showDesugaredAst)
    .argType(showArgsType)
    .displayName("Show desugared AST")
    .description("Shows the desugared Abstract Syntax Tree of the program.")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.ResourceKey)),
      ParamRepr.of("region", TypeInfo.of("mb.common.region", "Region"), false, ArgProviderRepr.context(CommandContextType.Region))
    ))
    .build()
  addCommandDefs(showDesugaredAstCommand)

  // Show analyzed AST
  val showAnalyzedAst = TypeInfo.of(taskPackageId, "TigerShowAnalyzedAst")
  addTaskDefs(showAnalyzedAst)
  val showAnalyzedAstCommand = CommandDefRepr.builder()
    .type(commandPackageId, "TigerShowAnalyzedAstCommand")
    .taskDefType(showAnalyzedAst)
    .argType(showArgsType)
    .displayName("Show analyzed AST")
    .description("Shows the analyzed Abstract Syntax Tree of the program.")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.ResourceKey)),
      ParamRepr.of("region", TypeInfo.of("mb.common.region", "Region"), false, ArgProviderRepr.context(CommandContextType.Region))
    ))
    .build()
  addCommandDefs(showAnalyzedAstCommand)

  // Show pretty-printed text
  val showPrettyPrintedText = TypeInfo.of(taskPackageId, "TigerShowPrettyPrintedText")
  addTaskDefs(showPrettyPrintedText)
  val showPrettyPrintedTextCommand = CommandDefRepr.builder()
    .type(commandPackageId, "TigerShowPrettyPrintedTextCommand")
    .taskDefType(showPrettyPrintedText)
    .argType(showArgsType)
    .displayName("Show pretty-printed text")
    .description("Shows a pretty-printed version of the program.")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.ResourceKey)),
      ParamRepr.of("region", TypeInfo.of("mb.common.region", "Region"), false, ArgProviderRepr.context(CommandContextType.Region))
    ))
    .build()
  addCommandDefs(showPrettyPrintedTextCommand)

  // Show scope graph
  val showScopeGraph = TypeInfo.of(taskPackageId, "TigerShowScopeGraph")
  addTaskDefs(showScopeGraph)
  val showScopeGraphCommand = CommandDefRepr.builder()
    .type(commandPackageId, "TigerShowScopeGraphCommand")
    .taskDefType(showScopeGraph)
    .argType(showArgsType)
    .displayName("Show scope graph")
    .description("Shows the scope graph for the program")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.ResourceKey)),
      ParamRepr.of("region", TypeInfo.of("mb.common.region", "Region"), false, ArgProviderRepr.value("null"))
    ))
//    .addAllArgs(listOf(
//      VarRepr.of("resource"),
//      ConstRepr.ofNull()
//    ))
    .build()
  addCommandDefs(showScopeGraphCommand)

  // Menu bindings
  val mainAndEditorMenu = listOf(
    MenuItemRepr.menu("Compile",
      MenuItemRepr.menu("Static Semantics",
        CommandActionRepr.builder().manualOnce(compileFileCommand).fileRequired().buildItem(),
        CommandActionRepr.builder().manualOnce(compileFileAltCommand, "- default").fileRequired().buildItem(),
        CommandActionRepr.builder().manualOnce(compileFileAltCommand, "- list literal values instead", mapOf("listDefNames" to "false", "compiledFileNameSuffix" to doubleQuote("litvals.aterm"))).fileRequired().buildItem(),
        CommandActionRepr.builder().manualOnce(compileFileAltCommand, "- base64 encode", mapOf("base64Encode" to "true", "compiledFileNameSuffix" to doubleQuote("defnames_base64.txt"))).fileRequired().buildItem(),
        CommandActionRepr.builder().manualOnce(compileFileAltCommand, "- list literal values instead + base64 encode", mapOf("listDefNames" to "false", "base64Encode" to "true", "compiledFileNameSuffix" to doubleQuote("litvals_base64.txt"))).fileRequired().buildItem(),
        CommandActionRepr.builder().manualContinuous(compileFileAltCommand, "- default").fileRequired().buildItem(),
        CommandActionRepr.builder().manualContinuous(compileFileAltCommand, "- list literal values instead", mapOf("listDefNames" to "false", "compiledFileNameSuffix" to doubleQuote("litvals.aterm"))).fileRequired().buildItem(),
        CommandActionRepr.builder().manualContinuous(compileFileAltCommand, "- base64 encode", mapOf("base64Encode" to "true", "compiledFileNameSuffix" to doubleQuote("defnames_base64.txt"))).fileRequired().buildItem(),
        CommandActionRepr.builder().manualContinuous(compileFileAltCommand, "- list literal values instead + base64 encode", mapOf("listDefNames" to "false", "base64Encode" to "true", "compiledFileNameSuffix" to doubleQuote("litvals_base64.txt"))).fileRequired().buildItem()
      )
    ),
    MenuItemRepr.menu("Debug",
      MenuItemRepr.menu("Syntax",
        CommandActionRepr.builder().manualOnce(showParsedAstCommand).buildItem(),
        CommandActionRepr.builder().manualContinuous(showParsedAstCommand).buildItem(),
        CommandActionRepr.builder().manualOnce(showPrettyPrintedTextCommand).buildItem(),
        CommandActionRepr.builder().manualContinuous(showPrettyPrintedTextCommand).buildItem()
      ),
      MenuItemRepr.menu("Static Semantics",
        CommandActionRepr.builder().manualOnce(showAnalyzedAstCommand).buildItem(),
        CommandActionRepr.builder().manualContinuous(showAnalyzedAstCommand).buildItem()
      ),
      MenuItemRepr.menu("Transformations",
        CommandActionRepr.builder().manualOnce(showDesugaredAstCommand).buildItem(),
        CommandActionRepr.builder().manualContinuous(showDesugaredAstCommand).buildItem()
      )
    )
  )
  addAllMainMenuItems(mainAndEditorMenu)
  addAllEditorContextMenuItems(mainAndEditorMenu)
  addResourceContextMenuItems(
    MenuItemRepr.menu("Compile",
      CommandActionRepr.builder().manualOnce(compileFileCommand).fileRequired().buildItem(),
      CommandActionRepr.builder().manualOnce(compileDirectoryCommand).directoryRequired().buildItem(),
      CommandActionRepr.builder().manualOnce(compileFileAltCommand, "- default").fileRequired().buildItem(),
      CommandActionRepr.builder().manualOnce(compileFileAltCommand, "- list literal values instead", mapOf("listDefNames" to "false", "compiledFileNameSuffix" to doubleQuote("litvals.aterm"))).fileRequired().buildItem(),
      CommandActionRepr.builder().manualOnce(compileFileAltCommand, "- base64 encode", mapOf("base64Encode" to "true", "compiledFileNameSuffix" to doubleQuote("defnames_base64.txt"))).fileRequired().buildItem(),
      CommandActionRepr.builder().manualOnce(compileFileAltCommand, "- list literal values instead + base64 encode", mapOf("listDefNames" to "false", "base64Encode" to "true", "compiledFileNameSuffix" to doubleQuote("litvals_base64.txt"))).fileRequired().buildItem(),
      CommandActionRepr.builder().manualContinuous(compileFileAltCommand, "- default").fileRequired().buildItem(),
      CommandActionRepr.builder().manualContinuous(compileFileAltCommand, "- list literal values instead", mapOf("listDefNames" to "false", "compiledFileNameSuffix" to doubleQuote("litvals.aterm"))).fileRequired().buildItem(),
      CommandActionRepr.builder().manualContinuous(compileFileAltCommand, "- base64 encode", mapOf("base64Encode" to "true", "compiledFileNameSuffix" to doubleQuote("defnames_base64.txt"))).fileRequired().buildItem(),
      CommandActionRepr.builder().manualContinuous(compileFileAltCommand, "- list literal values instead + base64 encode", mapOf("listDefNames" to "false", "base64Encode" to "true", "compiledFileNameSuffix" to doubleQuote("litvals_base64.txt"))).fileRequired().buildItem()
    ),
    MenuItemRepr.menu("Debug",
      MenuItemRepr.menu("Syntax",
        CommandActionRepr.builder().manualOnce(showParsedAstCommand).buildItem(),
        CommandActionRepr.builder().manualOnce(showPrettyPrintedTextCommand).buildItem()
      ),
      MenuItemRepr.menu("Static Semantics",
        CommandActionRepr.builder().manualOnce(showAnalyzedAstCommand).buildItem()
      ),
      MenuItemRepr.menu("Transformations",
        CommandActionRepr.builder().manualOnce(showDesugaredAstCommand).buildItem()
      )
    )
  )
}
