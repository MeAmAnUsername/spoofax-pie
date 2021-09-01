package pl.thesis.evaluation.tasks;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class ProjectEvaluationResult {
    @NonNull public final ProjectCounts projectCounts;
    @NonNull public final TaskCounts taskCounts;

    public ProjectEvaluationResult(@NonNull ProjectCounts projectCounts, @NonNull TaskCounts taskCounts) {
        this.projectCounts = projectCounts;
        this.taskCounts = taskCounts;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ProjectEvaluationResult that = (ProjectEvaluationResult)o;
        return projectCounts.equals(that.projectCounts) && taskCounts.equals(that.taskCounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectCounts, taskCounts);
    }

    @Override public String toString() {
        return "ProjectEvaluationResult{" +
            "projectCounts=" + projectCounts +
            ", taskCounts=" + taskCounts +
            '}';
    }
}
