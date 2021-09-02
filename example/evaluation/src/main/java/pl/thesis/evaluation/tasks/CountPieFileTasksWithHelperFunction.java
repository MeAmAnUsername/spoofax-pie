package pl.thesis.evaluation.tasks;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountPieFileTasksWithHelperFunction implements TaskDef<@NonNull Supplier<@NonNull String>, @NonNull Integer> {
    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Integer exec(@NonNull ExecContext context, @NonNull Supplier<@NonNull String> input) {
        return countTasksWithHelperFunction(context.require(input));
    }

    public static int countTasksWithHelperFunction(String program) {
        final Matcher matcher = Pattern.compile("// uses java helper function").matcher(program);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
