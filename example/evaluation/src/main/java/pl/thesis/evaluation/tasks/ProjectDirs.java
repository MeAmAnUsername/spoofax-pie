package pl.thesis.evaluation.tasks;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;

public class ProjectDirs implements Serializable {
    @NonNull public final Path javaProject;
    @NonNull public final Path oldPieProject;
    @NonNull public final Path newPieProject;

    public ProjectDirs(@NonNull Path javaProject, @NonNull Path oldPieProject, @NonNull Path newPieProject) {
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
