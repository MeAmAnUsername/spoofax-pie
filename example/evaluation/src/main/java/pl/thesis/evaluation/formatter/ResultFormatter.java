package pl.thesis.evaluation.formatter;

import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;

public interface ResultFormatter {
    @NonNull String format(@NonNull EvaluationResult evaluationResult);

    // todo: move formatting code from EvaluationResult to here
}
