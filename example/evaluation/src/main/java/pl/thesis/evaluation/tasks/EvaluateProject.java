package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EvaluateProject implements TaskDef<@NonNull ResourcePath, @NonNull Result<@NonNull ProjectEvaluationResult, @NonNull Exception>> {
    @NonNull private final CountLinesAndCharacters countLinesAndCharacters;

    public EvaluateProject(@NonNull CountLinesAndCharacters countLinesAndCharacters) {
        this.countLinesAndCharacters = countLinesAndCharacters;
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

        //noinspection ConstantConditions  Safe because isErr() returned false
        return Result.ofOk(new ProjectEvaluationResult(characterCounts.get()));
    }
}
