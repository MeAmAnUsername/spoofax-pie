package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CountTasks implements TaskDef<@NonNull ResourcePath, @NonNull Result<@NonNull TaskCounts, @NonNull Exception>> {
    @NonNull private final CountTaskDefs countTaskDefs;

    public CountTasks(@NonNull CountTaskDefs countTaskDefs) {
        this.countTaskDefs = countTaskDefs;
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Result<@NonNull TaskCounts, @NonNull Exception> exec(@NonNull ExecContext context, @NonNull ResourcePath input) {
        final Result<@NonNull Integer, @NonNull Exception> taskDefs = context.require(countTaskDefs.createTask(input));
        if (taskDefs.isErr()) {
            //noinspection ConstantConditions  safe because isErr returned true
            return Result.ofErr(taskDefs.getErr());
        }
        //noinspection ConstantConditions  safe because isErr returned false
        return Result.ofOk(new TaskCounts(taskDefs.get()));
    }
}
