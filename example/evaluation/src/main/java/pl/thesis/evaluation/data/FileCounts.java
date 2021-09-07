package pl.thesis.evaluation.data;

import java.io.Serializable;
import java.util.Objects;

public class FileCounts implements Serializable {
    public final int linesIncludingLayout;
    public final int linesExcludingLayout;
    public final int charsIncludingLayout;
    public final int charsExcludingLayout;

    public FileCounts(int linesIncludingLayout, int linesExcludingLayout, int charsIncludingLayout, int charsExcludingLayout) {
        this.linesIncludingLayout = linesIncludingLayout;
        this.linesExcludingLayout = linesExcludingLayout;
        this.charsIncludingLayout = charsIncludingLayout;
        this.charsExcludingLayout = charsExcludingLayout;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        FileCounts that = (FileCounts)o;
        return linesIncludingLayout == that.linesIncludingLayout && linesExcludingLayout == that.linesExcludingLayout && charsIncludingLayout == that.charsIncludingLayout && charsExcludingLayout == that.charsExcludingLayout;
    }

    @Override
    public int hashCode() {
        return Objects.hash(linesIncludingLayout, linesExcludingLayout, charsIncludingLayout, charsExcludingLayout);
    }

    @Override public String toString() {
        return "FileCounts{" +
            "linesIncludingLayout=" + linesIncludingLayout +
            ", linesExcludingLayout=" + linesExcludingLayout +
            ", charsIncludingLayout=" + charsIncludingLayout +
            ", charsExcludingLayout=" + charsExcludingLayout +
            '}';
    }
}
