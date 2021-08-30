package pl.thesis.evaluation.tasks;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class ProjectCounts implements Serializable {
    @NonNull public final FileCounts javaCounts;
    @NonNull public final FileCounts pieCounts;

    public ProjectCounts(@NonNull FileCounts javaCounts, @NonNull FileCounts pieCounts) {
        this.javaCounts = javaCounts;
        this.pieCounts = pieCounts;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ProjectCounts that = (ProjectCounts)o;
        return javaCounts.equals(that.javaCounts) && pieCounts.equals(that.pieCounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaCounts, pieCounts);
    }

    @Override public String toString() {
        return "ProjectCounts{" +
            "javaCounts=" + javaCounts +
            ", pieCounts=" + pieCounts +
            '}';
    }
}
