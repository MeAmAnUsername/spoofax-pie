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
            final String contents = file.readString(StandardCharsets.UTF_8);
            if (contents.contains("implements TaskDef<")) {
                return Result.ofOk(true);
            }

            final String extendsKeyword = "extends "; // with trailing space
            final int extendsIndex = contents.indexOf(extendsKeyword) + extendsKeyword.length();
            final int newlineIndex = contents.indexOf('\n', extendsIndex);
            final boolean indirectlyImplements = extendsIndex > -1 && newlineIndex > -1 &&
                contents.substring(extendsIndex, newlineIndex).matches("[a-zA-Z0-9_]+?TaskDef \\{");
            return Result.ofOk(indirectlyImplements);
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }
}
