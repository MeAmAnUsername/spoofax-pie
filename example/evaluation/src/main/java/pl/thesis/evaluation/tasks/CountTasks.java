package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

public class CountTasks implements TaskDef<@NonNull ResourcePath, @NonNull Result<@NonNull TaskCounts, @NonNull IOException>> {
    @NonNull private final CountTaskDefs countTaskDefs;
    @NonNull private final CountPieTasks countPieTasks;

    public CountTasks(@NonNull CountTaskDefs countTaskDefs, @NonNull CountPieTasks countPieTasks) {
        this.countTaskDefs = countTaskDefs;
        this.countPieTasks = countPieTasks;
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Result<@NonNull TaskCounts, @NonNull IOException> exec(@NonNull ExecContext context, @NonNull ResourcePath input) {
        final Result<@NonNull Integer, @NonNull IOException> javaTasks = context.require(countTaskDefs.createTask(input));
        if (javaTasks.isErr()) {
            //noinspection ConstantConditions  safe because isErr returned true
            return Result.ofErr(javaTasks.getErr());
        }
        final Result<@NonNull Integer, @NonNull IOException> pieTasks = context.require(countPieTasks.createTask(input));
        if (pieTasks.isErr()) {
            //noinspection ConstantConditions  safe because isErr returned true
            return Result.ofErr(pieTasks.getErr());
        }

        //noinspection ConstantConditions  safe because isErr returned false
        return Result.ofOk(new TaskCounts(javaTasks.get(), pieTasks.get()));
    }
}
