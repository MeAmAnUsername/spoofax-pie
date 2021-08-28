package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CountLines implements TaskDef<@NonNull ResourcePath, @NonNull Result<LineCounts, @NonNull Exception>> {
    @NonNull private final CountFileLines countFileLines;

    public CountLines(@NonNull CountFileLines countFileLines) {
        this.countFileLines = countFileLines;
    }

    @java.lang.Override
    public @NonNull String getId() {
        return getClass().getSimpleName();
    }

    @java.lang.Override
    public @NonNull Result<LineCounts, @NonNull Exception> exec(ExecContext context, @NonNull ResourcePath input) {
        try {
            AtomicInteger javaLineCounts = new AtomicInteger();
            List<IOException> errors = new ArrayList<>();
            context.getHierarchicalResource(input)
                .walk(ResourceMatcher.ofFileExtension("java"))
                .map(file -> context.require(countFileLines.createTask(file.getPath())))
                .forEach(result -> result.ifElse(javaLineCounts::addAndGet, errors::add));
            // todo: count pie lines as well
            if (!errors.isEmpty()) {
                return Result.ofErr(new CompositeException(errors));
            }
            return Result.ofOk(new LineCounts(javaLineCounts.get(), 0));
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }
}
