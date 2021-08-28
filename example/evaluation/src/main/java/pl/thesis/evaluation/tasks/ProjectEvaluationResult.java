package pl.thesis.evaluation.tasks;

import java.util.Objects;

public class ProjectEvaluationResult {
    public final LineCounts lineCounts;

    public ProjectEvaluationResult(LineCounts lineCounts) {
        this.lineCounts = lineCounts;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ProjectEvaluationResult that = (ProjectEvaluationResult)o;
        return Objects.equals(lineCounts, that.lineCounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineCounts);
    }

    @Override public String toString() {
        return "ProjectEvaluationResult{" +
            "lineCounts=" + lineCounts +
            '}';
    }
}
