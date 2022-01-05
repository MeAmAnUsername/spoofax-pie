package pl.thesis.evaluation.formatter;

import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;

import java.util.Arrays;
import java.util.List;

public class LatexTableResultFormatter implements ResultFormatter {
    @Override
    public @NonNull String format(@NonNull EvaluationResult evaluationResult) {
        final String rowStart = "  ";
        final String separator = " & ";
        final String rowEnd = " \\\\\n";
        //noinspection SpellCheckingInspection  hline is a Latex command
        final String separatorRow = rowStart + "\\hline\n";
        String preamble = "\\begin{tabular}{ |l|r|r|r|r| }\n";
        String postamble = "\\end{tabular}\n";
        final List<Column> columns = getColumns(evaluationResult);
        final List<ResultFormatter.Row> rows = getRows();
        return ResultFormatter.formatTable(rowStart, separator, rowEnd, separatorRow, preamble, postamble, columns, rows);
    }

    public List<Column> getColumns(EvaluationResult evaluationResult) {
        return Arrays.asList(
            new Column("Value", 56, rowProducer -> rowProducer.name, true),
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
