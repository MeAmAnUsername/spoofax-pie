package pl.thesis.evaluation.tasks;

import java.util.Objects;

public class TaskCounts {
    public final int javaTasks;

    public TaskCounts(int javaTasks) {
        this.javaTasks = javaTasks;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        TaskCounts that = (TaskCounts)o;
        return javaTasks == that.javaTasks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaTasks);
    }

    @Override public String toString() {
        return "TaskCounts{" +
            "javaTasks=" + javaTasks +
            '}';
    }
}
