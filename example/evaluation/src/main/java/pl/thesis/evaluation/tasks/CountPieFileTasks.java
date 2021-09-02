package pl.thesis.evaluation.tasks;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountPieFileTasks implements TaskDef<@NonNull Supplier<@NonNull String>, @NonNull Integer> {
    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Integer exec(@NonNull ExecContext context, @NonNull Supplier<@NonNull String> input) {
        return countTasks(context.require(input));
    }

    public static int countTasks(String fileContents) {
        // Count tasks by looking for the keyword 'func', '(' and '=', not followed by the keyword foreign
        final String pattern = "\\Wfunc\\W[^(]*\\([^=]+=(?!\\W*?foreign\\W)";
        final Matcher matcher = Pattern.compile(pattern).matcher(fileContents);
        int count = 0;
        while(matcher.find()) {
            count++;
        }
        return count;
    }
}
