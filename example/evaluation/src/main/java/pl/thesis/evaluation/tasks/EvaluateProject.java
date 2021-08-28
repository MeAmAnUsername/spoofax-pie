package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EvaluateProject implements TaskDef<@NonNull ResourcePath, @NonNull Result<ProjectEvaluationResult, @NonNull Exception>> {
    private final CountLines countLines;

    public EvaluateProject(CountLines countLines) {
        this.countLines = countLines;
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    @NonNull
    public Result<ProjectEvaluationResult, @NonNull Exception> exec(ExecContext context, @NonNull ResourcePath input) {
        Result<LineCounts, @NonNull Exception> lineCounts = context.require(countLines.createTask(input));
        if (lineCounts.isErr()) {
            //noinspection ConstantConditions  Safe because isErr() returned true
            return Result.ofErr(lineCounts.getErr());
        }

        return Result.ofOk(new ProjectEvaluationResult(lineCounts.get()));
    }
}
