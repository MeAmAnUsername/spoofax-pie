package pl.thesis.evaluation.tasks;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Pattern;

public class IsTaskDef implements TaskDef<@NonNull Supplier<@NonNull String>, @NonNull Boolean> {

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Boolean exec(@NonNull ExecContext context, @NonNull Supplier<@NonNull String> input) {
        return isTaskDef(context.require(input));
    }

    public static boolean isTaskDef(String javaFile) {
        return javaFile.contains("implements TaskDef<") ||
            Pattern.compile("extends [a-zA-Z0-9_]+?TaskDef \\{").matcher(javaFile).find();
    }
}
