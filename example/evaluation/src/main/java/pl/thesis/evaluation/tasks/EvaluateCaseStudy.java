package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.fs.FSPath;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EvaluateCaseStudy implements TaskDef<@NonNull ProjectDirs, @NonNull Result<@NonNull EvaluationResult, @NonNull Exception>> {
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
    public @NonNull Result<@NonNull EvaluationResult, @NonNull Exception> exec(@NonNull ExecContext context, @NonNull ProjectDirs input) {
        return Result.ofOkOrCatching(() -> new EvaluationResult(
            context.require(evaluateProject.createTask(new FSPath(input.javaProject))).unwrap(),
            context.require(evaluateProject.createTask(new FSPath(input.oldPieProject))).unwrap(),
            context.require(evaluateProject.createTask(new FSPath(input.newPieProject))).unwrap()
        ), Exception.class);
    }
}
