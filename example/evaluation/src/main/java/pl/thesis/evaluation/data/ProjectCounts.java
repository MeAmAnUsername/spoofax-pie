package pl.thesis.evaluation.data;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class ProjectCounts implements Serializable {
    @NonNull public final FileCounts javaCounts;
    @NonNull public final FileCounts pieCounts;
    @NonNull public final FileCounts pieLibraryCounts;

    public ProjectCounts(@NonNull FileCounts javaCounts, @NonNull FileCounts pieCounts, @NonNull FileCounts pieLibraryCounts) {
        this.javaCounts = javaCounts;
        this.pieCounts = pieCounts;
        this.pieLibraryCounts = pieLibraryCounts;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ProjectCounts that = (ProjectCounts)o;
        return javaCounts.equals(that.javaCounts) && pieCounts.equals(that.pieCounts) && pieLibraryCounts.equals(that.pieLibraryCounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaCounts, pieCounts, pieLibraryCounts);
    }

    @Override public String toString() {
        return "ProjectCounts{" +
            "javaCounts=" + javaCounts +
            ", pieCounts=" + pieCounts +
            ", pieLibraryCounts=" + pieLibraryCounts +
            '}';
    }
}
