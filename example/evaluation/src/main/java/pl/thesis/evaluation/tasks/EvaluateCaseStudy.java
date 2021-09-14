package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;
import pl.thesis.evaluation.data.Projects;

public class EvaluateCaseStudy implements TaskDef<@NonNull Projects, @NonNull Result<@NonNull EvaluationResult, @NonNull Exception>> {
    @NonNull private final EvaluateProject evaluateProject;

    public EvaluateCaseStudy(@NonNull EvaluateProject evaluateProject) {
        this.evaluateProject = evaluateProject;
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Result<@NonNull EvaluationResult, @NonNull Exception> exec(@NonNull ExecContext context, @NonNull Projects input) {
        return Result.ofOkOrCatching(() -> new EvaluationResult(
            context.require(evaluateProject.createTask(input.javaProject)).unwrap(),
            context.require(evaluateProject.createTask(input.oldPieProject)).unwrap(),
            context.require(evaluateProject.createTask(input.newPieProject)).unwrap()
        ), Exception.class);
    }
}
