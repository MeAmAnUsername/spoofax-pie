package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EvaluateProject implements TaskDef<@NonNull ResourcePath, @NonNull Result<@NonNull ProjectEvaluationResult, @NonNull Exception>> {
    @NonNull private final CountLines countLines;
    @NonNull private final CountCharacters countCharacters;

    public EvaluateProject(@NonNull CountLines countLines, @NonNull CountCharacters countCharacters) {
        this.countLines = countLines;
        this.countCharacters = countCharacters;
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    @NonNull
    public Result<@NonNull ProjectEvaluationResult, @NonNull Exception> exec(ExecContext context, @NonNull ResourcePath input) {
        Result<LineCounts, @NonNull Exception> lineCounts = context.require(countLines.createTask(input));
        if (lineCounts.isErr()) {
            //noinspection ConstantConditions  Safe because isErr() returned true
            return Result.ofErr(lineCounts.getErr());
        }
        Result<ProjectCounts, @NonNull Exception> characterCounts = context.require(countCharacters.createTask(input));
        if (characterCounts.isErr()) {
            //noinspection ConstantConditions  Safe because isErr() returned true
            return Result.ofErr(characterCounts.getErr());
        }

        //noinspection ConstantConditions  Safe because isErr() returned false
        return Result.ofOk(new ProjectEvaluationResult(lineCounts.get(), characterCounts.get()));
    }
}
