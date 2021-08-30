package pl.thesis.evaluation.tasks;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class ProjectEvaluationResult {
    @NonNull public final LineCounts lineCounts;
    @NonNull public final ProjectCounts characterCounts;

    public ProjectEvaluationResult(@NonNull LineCounts lineCounts, @NonNull ProjectCounts characterCounts) {
        this.lineCounts = lineCounts;
        this.characterCounts = characterCounts;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ProjectEvaluationResult that = (ProjectEvaluationResult)o;
        return lineCounts.equals(that.lineCounts) && characterCounts.equals(that.characterCounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineCounts, characterCounts);
    }

    @Override public String toString() {
        return "ProjectEvaluationResult{" +
            "lineCounts=" + lineCounts +
            ", characterCounts=" + characterCounts +
            '}';
    }
}
