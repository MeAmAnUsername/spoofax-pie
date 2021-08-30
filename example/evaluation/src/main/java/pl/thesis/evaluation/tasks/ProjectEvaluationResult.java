package pl.thesis.evaluation.tasks;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class ProjectEvaluationResult {
    @NonNull public final ProjectCounts projectCounts;

    public ProjectEvaluationResult(@NonNull ProjectCounts projectCounts) {
        this.projectCounts = projectCounts;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ProjectEvaluationResult that = (ProjectEvaluationResult)o;
        return projectCounts.equals(that.projectCounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectCounts);
    }

    @Override public String toString() {
        return "ProjectEvaluationResult{" +
            "projectCounts=" + projectCounts +
            '}';
    }
}
