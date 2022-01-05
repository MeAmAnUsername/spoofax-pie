package pl.thesis.evaluation.formatter;

import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;

import java.util.Arrays;
import java.util.List;

import static pl.thesis.evaluation.formatter.ResultFormatter.RowProducer.ofIntFunction;

public class LatexOverviewTableResultFormatter implements ResultFormatter {
    @Override
    public @NonNull String format(@NonNull EvaluationResult evaluationResult) {
        final String rowStart = "  ";
        final String separator = " & ";
        final String rowEnd = " \\\\\n";
        //noinspection SpellCheckingInspection  hline is a Latex command
        final String separatorRow = rowStart + "\\hline\n";
        String preamble = "\\begin{tabular}{ |l|r|r| }\n";
        String postamble = "\\end{tabular}\n";
        final List<Column> columns = getColumns(evaluationResult);
        final List<ResultFormatter.Row> rows = getRows();
        return ResultFormatter.formatTable(rowStart, separator, rowEnd, separatorRow, preamble, postamble, columns, rows);
    }

    public List<Column> getColumns(EvaluationResult evaluationResult) {
        return Arrays.asList(
            new Column("Value", 56, rowProducer -> rowProducer.name, true),
            new Column("Difference", 10, rowProducer -> Column.absoluteDiff(rowProducer, evaluationResult.newPieResult, evaluationResult.javaResult), false),
            new Column("Difference (\\%)", 15, rowProducer -> escapePercent(Column.percentageDiff(rowProducer, evaluationResult.javaResult, evaluationResult.newPieResult)), false)
        );
    }

    public List<Row> getRows() {
        return Arrays.asList(
            ofIntFunction("total lines including libraries and layout" , result -> result.projectCounts.javaCounts.linesIncludingLayout+result.projectCounts.pieCounts.linesIncludingLayout),
            ofIntFunction("total lines including libraries, excluding layout" , result -> result.projectCounts.javaCounts.linesExcludingLayout+result.projectCounts.pieCounts.linesExcludingLayout),
            ofIntFunction("total lines excluding libraries, including layout" , result -> result.projectCounts.javaCounts.linesIncludingLayout+result.projectCounts.pieCounts.linesIncludingLayout-result.projectCounts.pieLibraryCounts.linesIncludingLayout),
            ofIntFunction("total lines excluding libraries and layout" , result -> result.projectCounts.javaCounts.linesExcludingLayout+result.projectCounts.pieCounts.linesExcludingLayout-result.projectCounts.pieLibraryCounts.linesExcludingLayout),
            new ResultFormatter.SeparatorRow(),
            ofIntFunction("total characters including libraries and layout" , result -> result.projectCounts.javaCounts.charsIncludingLayout+result.projectCounts.pieCounts.charsIncludingLayout),
            ofIntFunction("total characters including libraries, excluding layout" , result -> result.projectCounts.javaCounts.charsExcludingLayout+result.projectCounts.pieCounts.charsExcludingLayout),
            ofIntFunction("total characters excluding libraries, including layout" , result -> result.projectCounts.javaCounts.charsIncludingLayout+result.projectCounts.pieCounts.charsIncludingLayout-result.projectCounts.pieLibraryCounts.charsIncludingLayout),
            ofIntFunction("total characters excluding libraries and layout" , result -> result.projectCounts.javaCounts.charsExcludingLayout+result.projectCounts.pieCounts.charsExcludingLayout-result.projectCounts.pieLibraryCounts.charsExcludingLayout)
        );
    }

    static @NonNull String escapePercent(String input) {
        return input.replace("%", "\\%");
    }
}
