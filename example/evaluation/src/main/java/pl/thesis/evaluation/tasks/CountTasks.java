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
    @NonNull private final CountPieTasksWithHelperFunction countPieTasksWithHelperFunction;

    public CountTasks(@NonNull CountTaskDefs countTaskDefs, @NonNull CountPieTasks countPieTasks, @NonNull CountPieTasksWithHelperFunction countPieTasksWithHelperFunction) {
        this.countTaskDefs = countTaskDefs;
        this.countPieTasks = countPieTasks;
        this.countPieTasksWithHelperFunction = countPieTasksWithHelperFunction;
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
        final Result<@NonNull Integer, @NonNull IOException> pieTasksWithHelperFunction =
            context.require(countPieTasksWithHelperFunction.createTask(input));
        if (pieTasksWithHelperFunction.isErr()) {
            //noinspection ConstantConditions  safe because isErr returned true
            return Result.ofErr(pieTasksWithHelperFunction.getErr());
        }

        //noinspection ConstantConditions  safe because isErr returned false
        return Result.ofOk(new TaskCounts(javaTasks.get(), pieTasks.get(), pieTasksWithHelperFunction.get()));
    }
}
