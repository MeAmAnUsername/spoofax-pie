package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.TaskCounts;

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
        return Result.ofOkOrCatching(() -> new TaskCounts(
            context.require(countTaskDefs.createTask(input)).unwrap(),
            context.require(countPieTasks.createTask(input)).unwrap(),
            context.require(countPieTasksWithHelperFunction.createTask(input)).unwrap()
        ), IOException.class);
    }
}
