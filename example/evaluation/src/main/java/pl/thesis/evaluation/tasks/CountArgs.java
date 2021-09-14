package pl.thesis.evaluation.tasks;

import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

public class CountArgs implements Serializable {
    public final @NonNull ResourcePath dir;
    public final @NonNull Collection<String> ownModules;

    public CountArgs(@NonNull ResourcePath dir, @NonNull Collection<String> ownModules) {
        this.dir = dir;
        this.ownModules = ownModules;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        CountArgs input = (CountArgs)o;
        return dir.equals(input.dir) && ownModules.equals(input.ownModules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dir, ownModules);
    }

    @Override public String toString() {
        return "Input{" +
            "dir=" + dir +
            ", ownModules=" + ownModules +
            '}';
    }
}
