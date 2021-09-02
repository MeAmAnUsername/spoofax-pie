package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ModifiedResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class CountPieTasks implements TaskDef<@NonNull ResourcePath, @NonNull Result<@NonNull Integer, @NonNull IOException>> {
    @NonNull private final CountPieFileTasks countPieFileTasks;

    public CountPieTasks(@NonNull CountPieFileTasks countPieFileTasks) {
        this.countPieFileTasks = countPieFileTasks;
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Result<@NonNull Integer, @NonNull IOException> exec(@NonNull ExecContext context, @NonNull ResourcePath input) throws Exception {
        try {
            final HierarchicalResource dir = context.require(input);
            dir.walkForEach(ResourceMatcher.ofDirectory(), context::require);

            final AtomicInteger count = new AtomicInteger();
            dir.walkForEach(ResourceMatcher.ofFileExtension("java"), file -> {
                final ResourceStringSupplier stringSupplier = new ResourceStringSupplier(file.getKey(),
                    new ModifiedResourceStamper<@NonNull ReadableResource>(), StandardCharsets.UTF_8);
                count.addAndGet(context.require(countPieFileTasks.createTask(stringSupplier)));
            });
            return Result.ofOk(count.get());
        } catch(IOException e) {
            return Result.ofErr(e);
        } catch(UncheckedIOException e) {
            return Result.ofErr(e.getCause());
        }
    }
}
