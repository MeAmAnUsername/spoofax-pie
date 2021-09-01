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

public class CountTaskDefs implements TaskDef<@NonNull ResourcePath, @NonNull Result<@NonNull Integer, @NonNull Exception>> {
    @NonNull private final IsTaskDef isTaskDef;

    public CountTaskDefs(@NonNull IsTaskDef isTaskDef) {
        this.isTaskDef = isTaskDef;
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Result<@NonNull Integer, @NonNull Exception> exec(@NonNull ExecContext context, @NonNull ResourcePath input) {
        try {
            final HierarchicalResource dir = context.require(input);
            dir.walkForEach(ResourceMatcher.ofDirectory(), context::require);

            final AtomicInteger count = new AtomicInteger();
            final List<IOException> errors = new ArrayList<>();
            dir.walk(ResourceMatcher.ofFileExtension("java"))
                .map(file -> context.require(isTaskDef.createTask(file.getPath())))
                .forEach(result -> result.ifElse(val -> count.addAndGet(val ? 1 : 0), errors::add));
            if (!errors.isEmpty()) {
                return Result.ofErr(new CompositeException(errors));
            }
            return Result.ofOk(count.get());
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }
}
