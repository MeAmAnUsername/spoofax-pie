package pl.thesis.evaluation.tasks;

import java.io.Serializable;

public class FileCounts implements Serializable {
    public final int includingLayout;
    public final int excludingLayout;

    public FileCounts(int includingLayout, int excludingLayout) {
        this.includingLayout = includingLayout;
        this.excludingLayout = excludingLayout;
    }

    public boolean equals(Object object) {
        if(this == object) return true;
        if(object == null || getClass() != object.getClass()) return false;
        if(!super.equals(object)) return false;
        FileCounts that = (FileCounts)object;
        return includingLayout == that.includingLayout && excludingLayout == that.excludingLayout;
    }

    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), includingLayout, excludingLayout);
    }

    @Override public String toString() {
        return "LineCounts{" +
            "includingLayout=" + includingLayout +
            ", excludingLayout=" + excludingLayout +
            '}';
    }
}
