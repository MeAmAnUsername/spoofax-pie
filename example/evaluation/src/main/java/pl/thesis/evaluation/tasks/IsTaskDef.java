package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ModifiedResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class IsTaskDef implements TaskDef<@NonNull ResourcePath, @NonNull Result<@NonNull Boolean, @NonNull IOException>> {

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Result<@NonNull Boolean, @NonNull IOException> exec(@NonNull ExecContext context, @NonNull ResourcePath input) {
        try {
            final ReadableResource file = context.require(input, new ModifiedResourceStamper<@NonNull ReadableResource>());
            return Result.ofOk(isTaskDef(file.readString(StandardCharsets.UTF_8)));
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }

    public static boolean isTaskDef(String javaFile) {
        return javaFile.contains("implements TaskDef<") ||
            Pattern.compile("extends [a-zA-Z0-9_]+?TaskDef \\{").matcher(javaFile).find();
    }
}
