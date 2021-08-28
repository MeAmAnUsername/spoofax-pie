package pl.thesis.evaluation.tasks;

import java.io.Serializable;

public class LineCounts implements Serializable {
    public final int javaLines;
    public final int pieLines;

    public LineCounts(int javaLines, int pieLines) {
        this.javaLines = javaLines;
        this.pieLines = pieLines;
    }

    public boolean equals(Object object) {
        if(this == object) return true;
        if(object == null || getClass() != object.getClass()) return false;
        if(!super.equals(object)) return false;
        LineCounts that = (LineCounts)object;
        return javaLines == that.javaLines && pieLines == that.pieLines;
    }

    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), javaLines, pieLines);
    }

    @Override public String toString() {
        return "LineCounts{" +
            "javaLines=" + javaLines +
            ", pieLines=" + pieLines +
            '}';
    }
}
