package pl.thesis.evaluation.formatter;

import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;

public class TextTableResultFormatter implements ResultFormatter {

    @Override
    public @NonNull String format(@NonNull EvaluationResult evaluationResult) {
        return evaluationResult.formatAsTable();
    }
}
