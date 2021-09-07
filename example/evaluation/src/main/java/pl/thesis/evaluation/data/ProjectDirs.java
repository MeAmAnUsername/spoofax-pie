package pl.thesis.evaluation.data;

import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class ProjectDirs implements Serializable {
    @NonNull public final ResourcePath javaProject;
    @NonNull public final ResourcePath oldPieProject;
    @NonNull public final ResourcePath newPieProject;

    public ProjectDirs(@NonNull ResourcePath javaProject, @NonNull ResourcePath oldPieProject, @NonNull ResourcePath newPieProject) {
        this.javaProject = javaProject;
        this.oldPieProject = oldPieProject;
        this.newPieProject = newPieProject;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ProjectDirs that = (ProjectDirs)o;
        return javaProject.equals(that.javaProject) && oldPieProject.equals(that.oldPieProject) && newPieProject.equals(that.newPieProject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaProject, oldPieProject, newPieProject);
    }

    @Override public String toString() {
        return "ProjectDirs{" +
            "javaProject=" + javaProject +
            ", oldPieProject=" + oldPieProject +
            ", newPieProject=" + newPieProject +
            '}';
    }
}
