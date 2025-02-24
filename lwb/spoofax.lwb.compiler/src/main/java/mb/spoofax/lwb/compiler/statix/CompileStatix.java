package mb.spoofax.lwb.compiler.statix;

import mb.cfg.metalang.CompileStatixInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.statix.task.StatixCheck;
import mb.statix.task.StatixCompile;
import mb.statix.task.StatixConfig;
import mb.statix.util.StatixUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

public class CompileStatix implements TaskDef<ResourcePath, Result<KeyedMessages, StatixCompileException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final ConfigureStatix configure;

    private final StatixCheck check;
    private final StatixCompile compile;

    @Inject public CompileStatix(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        ConfigureStatix configure,
        StatixCheck check,
        StatixCompile compile
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.configure = configure;
        this.check = check;
        this.compile = compile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, StatixCompileException> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(StatixCompileException::getLanguageCompilerConfigurationFail)
            .flatMapThrowing(o1 -> Option.ofOptional(o1.compileLanguageInput.compileLanguageSpecificationInput().statix()).mapThrowingOr(
                i -> context.require(configure, rootDirectory)
                    .mapErr(StatixCompileException::configureFail)
                    .flatMapThrowing(o2 -> o2.mapThrowingOr(
                        c -> checkAndCompile(context, c, i),
                        Result.ofOk(KeyedMessages.of())
                    )),
                Result.ofOk(KeyedMessages.of())
            ));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<KeyedMessages, StatixCompileException> checkAndCompile(ExecContext context, StatixConfig config, CompileStatixInput input) throws IOException {
        final KeyedMessages messages = context.require(check, config);
        if(messages.containsError()) {
            return Result.ofErr(StatixCompileException.checkFail(messages));
        }

        final ResourceWalker walker = StatixUtil.createResourceWalker();
        final ResourceMatcher matcher = StatixUtil.createResourceMatcher();
        final HierarchicalResource outputDirectory = context.getHierarchicalResource(input.outputDirectory()).ensureDirectoryExists();
        for(ResourcePath sourceOrIncludeDirectory : config.sourceAndIncludePaths()) {
            final HierarchicalResource directory = context.require(sourceOrIncludeDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
            try(final Stream<? extends HierarchicalResource> stream = directory.walk(walker, matcher)) {
                for(HierarchicalResource inputFile : new StreamIterable<>(stream)) {
                    final Result<StatixCompile.Output, ?> result = context.require(compile, new StatixCompile.Input(inputFile.getPath(), config));
                    if(result.isErr()) {
                        return Result.ofErr(StatixCompileException.compileFail(result.unwrapErr()));
                    }
                    final StatixCompile.Output output = result.unwrapUnchecked();
                    final HierarchicalResource outputFile = outputDirectory.appendAsRelativePath(output.relativeOutputPath).ensureFileExists();
                    outputFile.writeString(output.spec.toString());
                    context.provide(outputFile);
                }
            }
        }

        return Result.ofOk(messages);
    }
}
