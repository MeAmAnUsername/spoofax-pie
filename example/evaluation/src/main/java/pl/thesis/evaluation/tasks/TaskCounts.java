package pl.thesis.evaluation.tasks;

import java.io.Serializable;
import java.util.Objects;

public class TaskCounts implements Serializable {
    public final int javaTasks;
    public final int pieTasks;
    public final int pieTasksWithHelperFunction;

    public TaskCounts(int javaTasks, int pieTasks, int pieTasksWithHelperFunction) {
        this.javaTasks = javaTasks;
        this.pieTasks = pieTasks;
        this.pieTasksWithHelperFunction = pieTasksWithHelperFunction;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        TaskCounts that = (TaskCounts)o;
        return javaTasks == that.javaTasks && pieTasks == that.pieTasks && pieTasksWithHelperFunction == that.pieTasksWithHelperFunction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaTasks, pieTasks, pieTasksWithHelperFunction);
    }

    @Override public String toString() {
        return "TaskCounts{" +
            "javaTasks=" + javaTasks +
            ", pieTasks=" + pieTasks +
            ", pieTasksWithHelperFunction=" + pieTasksWithHelperFunction +
            '}';
    }
}
