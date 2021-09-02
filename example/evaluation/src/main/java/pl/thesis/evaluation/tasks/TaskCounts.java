package pl.thesis.evaluation.tasks;

import java.io.Serializable;
import java.util.Objects;

public class TaskCounts implements Serializable {
    public final int javaTasks;
    public final int pieTasks;

    public TaskCounts(int javaTasks, int pieTasks) {
        this.javaTasks = javaTasks;
        this.pieTasks = pieTasks;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        TaskCounts that = (TaskCounts)o;
        return javaTasks == that.javaTasks && pieTasks == that.pieTasks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaTasks, pieTasks);
    }

    @Override public String toString() {
        return "TaskCounts{" +
            "javaTasks=" + javaTasks +
            ", pieTasks=" + pieTasks +
            '}';
    }
}
