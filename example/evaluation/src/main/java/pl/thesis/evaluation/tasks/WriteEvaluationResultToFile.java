package pl.thesis.evaluation.tasks;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;
import pl.thesis.evaluation.formatter.ResultFormatter;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class WriteEvaluationResultToFile implements TaskDef<WriteEvaluationResultToFile.@NonNull Input, @NonNull None> {
    public static class Input implements Serializable {
        @NonNull public final ResultFormatter formatter;
        @NonNull public final ResourcePath file;
        @NonNull public final EvaluationResult evaluationResult;

        public Input(@NonNull ResultFormatter formatter, @NonNull ResourcePath file, @NonNull EvaluationResult evaluationResult) {
            this.formatter = formatter;
            this.file = file;
            this.evaluationResult = evaluationResult;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return formatter.equals(input.formatter) && file.equals(input.file) && evaluationResult.equals(input.evaluationResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(formatter, file, evaluationResult);
        }

        @Override public String toString() {
            return "Input{" +
                "formatter=" + formatter +
                ", file=" + file +
                ", evaluationResult=" + evaluationResult +
                '}';
        }
    }

    @NonNull public final ResourceService resourceService;

    public WriteEvaluationResultToFile(@NonNull ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull None exec(@NonNull ExecContext context, @NonNull Input input) throws Exception {
        final HierarchicalResource file = resourceService.getHierarchicalResource(input.file);
        file.ensureFileExists();
        file.writeString(input.formatter.format(input.evaluationResult), StandardCharsets.UTF_8);
        context.provide(file, ResourceStampers.hashFile());
        return None.instance;
    }
}
