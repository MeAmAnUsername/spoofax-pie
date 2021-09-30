package pl.thesis.evaluation.formatter;

import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TextTableResultFormatter implements ResultFormatter {

    @Override
    public @NonNull String format(@NonNull EvaluationResult evaluationResult) {
        final int indentation = 2;

        StringBuilder sb = new StringBuilder();
        ResultFormatter.appendSpaces(sb, indentation);
        sb.append("| ");
        final String rowStart = sb.toString();
        final String separator = " | ";
        final String rowEnd = " |\n";

        final List<Column> columns = getColumns(evaluationResult);

        // separatorRow
        sb = new StringBuilder();
        ResultFormatter.appendSpaces(sb, indentation);
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

        // header
        sb = new StringBuilder("Evaluation result\n");
        sb.append(separatorRow);
        sb.append(rowStart);
        first.set(true);
        for(Column column : columns) {
            if (!first.get()) {
                sb.append(separator);
            }
            column.appendHeader(sb);
            first.set(false);
        }
        sb.append(rowEnd);
        sb.append(separatorRow);

        // rest of the table
        for(Row row : ResultFormatter.getRows()) {
            if (row instanceof SeparatorRow) {
                sb.append(separatorRow);
                continue;
            }
            RowProducer rowProducer = (RowProducer)row;
            sb.append(rowStart);
            first.set(true);
            for(Column column : columns) {
                if (!first.get()) {
                    sb.append(separator);
                }
                column.appendCell(sb, rowProducer);
                first.set(false);
            }
            sb.append(rowEnd);
        }

        sb.append(separatorRow);
        return sb.toString();
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
