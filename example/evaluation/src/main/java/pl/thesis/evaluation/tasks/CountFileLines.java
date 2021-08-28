package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;

public class CountFileLines implements TaskDef<@NonNull ResourcePath, @NonNull Result<Integer, @NonNull IOException>> {
    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    @NonNull
    public Result<Integer, @NonNull IOException> exec(@NonNull ExecContext context, @NonNull ResourcePath input) {
        try(InputStream inputStream = context.getReadableResource(input).openRead()) {
            int lineCount = 0;
            int lastChar = '\n';
            int b;
            while((b = inputStream.read()) != -1) {
                lastChar = b;
                if (b == '\n') {
                    lineCount++;
                }
            }
            assert(inputStream.read() == -1);
            if (lastChar != '\n') {
                // a file with x lines only has x-1 newline characters, so add 1
                // Exception: do not count a trailing newline as a line (do not add 1 if file has a trailing newline)
                lineCount++;
            }
            return Result.ofOk(lineCount);
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }
}
