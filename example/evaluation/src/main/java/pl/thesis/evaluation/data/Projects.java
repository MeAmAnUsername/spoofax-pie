package pl.thesis.evaluation.data;

import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

public class Projects implements Serializable {
    @NonNull public final CountArgs javaProject;
    @NonNull public final CountArgs oldPieProject;
    @NonNull public final CountArgs newPieProject;

    public Projects(@NonNull CountArgs javaProject, @NonNull CountArgs oldPieProject, @NonNull CountArgs newPieProject) {
        this.javaProject = javaProject;
        this.oldPieProject = oldPieProject;
        this.newPieProject = newPieProject;
    }

    public Projects(@NonNull ResourcePath javaProject, @NonNull ResourcePath oldPieProject, @NonNull ResourcePath newPieProject, Collection<String> ownModules) {
        this.javaProject = new CountArgs(javaProject, ownModules);
        this.oldPieProject = new CountArgs(oldPieProject, ownModules);
        this.newPieProject = new CountArgs(newPieProject, ownModules);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Projects that = (Projects)o;
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
