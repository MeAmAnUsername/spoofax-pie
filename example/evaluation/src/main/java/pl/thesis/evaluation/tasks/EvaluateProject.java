package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.ProjectEvaluationResult;

public class EvaluateProject implements TaskDef<@NonNull CountArgs, @NonNull Result<@NonNull ProjectEvaluationResult, @NonNull Exception>> {
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
    public Result<@NonNull ProjectEvaluationResult, @NonNull Exception> exec(@NonNull ExecContext context, @NonNull CountArgs input) {
        return Result.ofOkOrCatching(() -> new ProjectEvaluationResult(
            context.require(countLinesAndCharacters.createTask(input)).unwrap(),
            context.require(countTasks.createTask(input.dir)).unwrap()
        ), Exception.class);
    }
}
