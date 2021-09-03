package pl.thesis.evaluation.tasks;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class WriteEvaluationResultToFile implements TaskDef<WriteEvaluationResultToFile.@NonNull Input, @NonNull None> {
    public static class Input implements Serializable {
        @NonNull public final EvaluationResult evaluationResult;
        @NonNull public final ResourcePath file;

        public Input(@NonNull EvaluationResult evaluationResult, @NonNull ResourcePath file) {
            this.evaluationResult = evaluationResult;
            this.file = file;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return evaluationResult.equals(input.evaluationResult) && file.equals(input.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(evaluationResult, file);
        }

        @Override public String toString() {
            return "Input{" +
                "evaluationResult=" + evaluationResult +
                ", file=" + file +
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
        file.writeString(input.evaluationResult.formatAsTable(), StandardCharsets.UTF_8);
        context.provide(file, ResourceStampers.hashFile());
        return None.instance;
    }


}
