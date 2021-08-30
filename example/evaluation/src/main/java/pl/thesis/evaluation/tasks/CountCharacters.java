package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CountCharacters implements TaskDef<@NonNull ResourcePath, @NonNull Result<@NonNull ProjectCounts, @NonNull Exception>> {
    @NonNull private final CountFileCharacters countFileCharacters;

    public CountCharacters(@NonNull CountFileCharacters countFileCharacters) {
        this.countFileCharacters = countFileCharacters;
    }

    @java.lang.Override
    public @NonNull String getId() {
        return getClass().getSimpleName();
    }

    @java.lang.Override
    public @NonNull Result<@NonNull ProjectCounts, @NonNull Exception> exec(ExecContext context, @NonNull ResourcePath input) {
        try {
            AtomicInteger javaCharacterCountsIncludingLayout = new AtomicInteger();
            AtomicInteger javaCharacterCountsExcludingLayout = new AtomicInteger();
            AtomicInteger pieCharacterCountsIncludingLayout = new AtomicInteger();
            AtomicInteger pieCharacterCountsExcludingLayout = new AtomicInteger();
            List<Exception> errors = new ArrayList<>();
            HierarchicalResource dir = context.require(input);
            dir.walkForEach(ResourceMatcher.ofDirectory(), context::require);
            dir.walk(ResourceMatcher.ofFileExtension("java"))
                .map(file -> context.require(countFileCharacters.createTask(file.getPath())))
                .forEach(result -> result.ifElse(counts -> {
                    javaCharacterCountsIncludingLayout.addAndGet(counts.includingLayout);
                    javaCharacterCountsExcludingLayout.addAndGet(counts.excludingLayout);
                }, errors::add));
            dir.walk(ResourceMatcher.ofFileExtension("pie"))
                .map(file -> context.require(countFileCharacters.createTask(file.getPath())))
                .forEach(result -> result.ifElse(counts -> {
                    pieCharacterCountsIncludingLayout.addAndGet(counts.includingLayout);
                    pieCharacterCountsExcludingLayout.addAndGet(counts.excludingLayout);
                }, errors::add));
            if (!errors.isEmpty()) {
                return Result.ofErr(new CompositeException(errors));
            }
            return Result.ofOk(new ProjectCounts(
                new FileCounts(javaCharacterCountsIncludingLayout.get(), javaCharacterCountsExcludingLayout.get()),
                new FileCounts(pieCharacterCountsIncludingLayout.get(), pieCharacterCountsExcludingLayout.get())
            ));
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }
}
