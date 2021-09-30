package pl.thesis.evaluation.formatter;

import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LatexTableResultFormatter implements ResultFormatter {
    @Override
    public @NonNull String format(@NonNull EvaluationResult evaluationResult) {
        final int indentation = 2;

        StringBuilder sb = new StringBuilder();
        ResultFormatter.appendSpaces(sb, indentation);
        final String rowStart = sb.toString();
        final String separator = " & ";
        final String rowEnd = " \\\\\n";
        final String separatorRow = rowStart + "\\hline\n";

        // header
        sb = new StringBuilder("\\begin{tabular}{ |c|c|c|c|c| }\n");
        sb.append(separatorRow);
        sb.append(rowStart);
        AtomicBoolean first = new AtomicBoolean(true);
        final List<Column> columns = getColumns(evaluationResult);
        // todo: merge with common part in TextTableResultFormatter
        for(Column column : columns) {
            if(!first.get()) {
                sb.append(separator);
            }
            column.appendHeader(sb);
            first.set(false);
        }
        sb.append(rowEnd);
        sb.append(separatorRow);

        // rest of the table
        for(Row row : ResultFormatter.getRows()) {
            if(row instanceof SeparatorRow) {
                sb.append(separatorRow);
                continue;
            }
            RowProducer rowProducer = (RowProducer)row;
            sb.append(rowStart);
            first.set(true);
            for(Column column : columns) {
                if(!first.get()) {
                    sb.append(separator);
                }
                column.appendCell(sb, rowProducer);
                first.set(false);
            }
            sb.append(rowEnd);
        }

        sb.append(separatorRow);
        sb.append("\\end{tabular}\n");
        return sb.toString();
    }

    static List<Column> getColumns(EvaluationResult evaluationResult) {
        return Arrays.asList(
            new Column("value", 56, rowProducer -> rowProducer.name, true),
            new Column("Java", 5, rowProducer -> Column.singleVal(rowProducer, evaluationResult.javaResult), false),
            new Column("PIE DSL", 7, rowProducer -> Column.singleVal(rowProducer, evaluationResult.newPieResult), false),
            new Column("Diff", 10, rowProducer -> Column.absoluteDiff(rowProducer, evaluationResult.newPieResult, evaluationResult.javaResult), false),
            new Column("Diff (\\%)", 10, rowProducer -> escapePercent(Column.percentageDiff(rowProducer, evaluationResult.javaResult, evaluationResult.newPieResult)), false)
        );
    }

    static @NonNull String escapePercent(String input) {
        return input.replace("%", "\\%");
    }
}
