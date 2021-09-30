package pl.thesis.evaluation.data;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class EvaluationResult implements Serializable {
    @NonNull public final ProjectEvaluationResult javaResult;
    @NonNull public final ProjectEvaluationResult oldPieResult;
    @NonNull public final ProjectEvaluationResult newPieResult;

    public EvaluationResult(@NonNull ProjectEvaluationResult javaResult,
                            @NonNull ProjectEvaluationResult oldPieResult,
                            @NonNull ProjectEvaluationResult newPieResult) {
        this.javaResult = javaResult;
        this.oldPieResult = oldPieResult;
        this.newPieResult = newPieResult;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        EvaluationResult that = (EvaluationResult)o;
        return javaResult.equals(that.javaResult) && oldPieResult.equals(that.oldPieResult) && newPieResult.equals(that.newPieResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaResult, oldPieResult, newPieResult);
    }

    @Override public String toString() {
        return "EvaluationResult{" +
            "javaResult=" + javaResult +
            ", oldPieResult=" + oldPieResult +
            ", newPieResult=" + newPieResult +
            '}';
    }
}
