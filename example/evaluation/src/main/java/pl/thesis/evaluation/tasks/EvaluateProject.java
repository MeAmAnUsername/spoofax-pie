package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

public class EvaluateProject implements TaskDef<@NonNull ResourcePath, @NonNull Result<@NonNull ProjectEvaluationResult, @NonNull Exception>> {
    @NonNull private final CountLinesAndCharacters countLinesAndCharacters;
    @NonNull private final CountTasks countTasks;

    public EvaluateProject(@NonNull CountLinesAndCharacters countLinesAndCharacters, @NonNull CountTasks countTasks) {
        this.countLinesAndCharacters = countLinesAndCharacters;
        this.countTasks = countTasks;
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    @NonNull
    public Result<@NonNull ProjectEvaluationResult, @NonNull Exception> exec(ExecContext context, @NonNull ResourcePath input) {
        Result<ProjectCounts, @NonNull Exception> characterCounts = context.require(countLinesAndCharacters.createTask(input));
        if (characterCounts.isErr()) {
            //noinspection ConstantConditions  Safe because isErr() returned true
            return Result.ofErr(characterCounts.getErr());
        }
        final Result<@NonNull TaskCounts, @NonNull IOException> taskCounts = context.require(countTasks.createTask(input));
        if (taskCounts.isErr()) {
            //noinspection ConstantConditions  Safe because isErr() returned true
            return Result.ofErr(taskCounts.getErr());
        }

        //noinspection ConstantConditions  Safe because isErr() returned false
        return Result.ofOk(new ProjectEvaluationResult(characterCounts.get(), taskCounts.get()));
    }
}
