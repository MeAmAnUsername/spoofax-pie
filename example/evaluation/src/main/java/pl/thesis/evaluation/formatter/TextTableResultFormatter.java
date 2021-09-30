package pl.thesis.evaluation.formatter;

import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TextTableResultFormatter implements ResultFormatter {

    @Override
    public @NonNull String format(@NonNull EvaluationResult evaluationResult) {
        final String indentation = "  ";
        final String rowStart = indentation + "| ";
        final String separator = " | ";
        final String rowEnd = " |\n";
        final String preamble = "Evaluation result\n";

        final List<Column> columns = getColumns(evaluationResult);

        // separatorRow
        StringBuilder sb = new StringBuilder();
        sb.append(indentation);
        sb.append("|-");
        AtomicBoolean first = new AtomicBoolean(true);
        for(Column column : columns) {
            if (!first.get()) {
                sb.append("-|-");
            }
            ResultFormatter.appendChars(sb, column.size, '-');
            first.set(false);
        }
        sb.append("-|\n");
        final String separatorRow = sb.toString();

        return ResultFormatter.formatTable(rowStart, separator, rowEnd, separatorRow, preamble, "", columns);
    }

    static List<Column> getColumns(EvaluationResult evaluationResult) {
        return Arrays.asList(
            new Column("value", 56, rowProducer -> rowProducer.name, true),
            new Column("Java", 5, rowProducer -> Column.singleVal(rowProducer, evaluationResult.javaResult), false),
            new Column("old PIE DSL", 11, rowProducer -> Column.singleVal(rowProducer, evaluationResult.oldPieResult), false),
            new Column("new PIE DSL", 11, rowProducer -> Column.singleVal(rowProducer, evaluationResult.newPieResult), false),
            new Column("Java vs. DSL", 12, rowProducer -> Column.absoluteDiff(rowProducer, evaluationResult.newPieResult, evaluationResult.javaResult), false),
            new Column("Java vs. DSL (%)", 16, rowProducer -> Column.percentageDiff(rowProducer, evaluationResult.javaResult, evaluationResult.newPieResult), false),
            new Column("old vs. new", 11, rowProducer -> Column.absoluteDiff(rowProducer, evaluationResult.newPieResult, evaluationResult.oldPieResult), false),
            new Column("old vs. new (%)", 15, rowProducer -> Column.percentageDiff(rowProducer, evaluationResult.oldPieResult, evaluationResult.newPieResult), false)
        );
    }
}
