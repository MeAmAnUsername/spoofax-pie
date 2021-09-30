package pl.thesis.evaluation.formatter;

import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.EvaluationResult;
import pl.thesis.evaluation.data.ProjectEvaluationResult;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static pl.thesis.evaluation.formatter.ResultFormatter.RowProducer.ofIntFunction;

public interface ResultFormatter {
    @NonNull String format(@NonNull EvaluationResult evaluationResult);

    static List<ResultFormatter.Row> getRows() {
        return Arrays.asList(
            ofIntFunction("java lines including layout", result -> result.projectCounts.javaCounts.linesIncludingLayout),
            ofIntFunction("java lines excluding layout", result -> result.projectCounts.javaCounts.linesExcludingLayout),
            ofIntFunction("PIE DSL lines including libraries and layout" , result -> result.projectCounts.pieCounts.linesIncludingLayout),
            ofIntFunction("PIE DSL lines excluding libraries, including layout" , result -> result.projectCounts.pieCounts.linesIncludingLayout-result.projectCounts.pieLibraryCounts.linesIncludingLayout),
            ofIntFunction("PIE DSL lines including libraries, excluding layout" , result -> result.projectCounts.pieCounts.linesExcludingLayout),
            ofIntFunction("PIE DSL lines excluding libraries and layout" , result -> result.projectCounts.pieCounts.linesExcludingLayout-result.projectCounts.pieLibraryCounts.linesExcludingLayout),
            ofIntFunction("total lines including libraries and layout" , result -> result.projectCounts.javaCounts.linesIncludingLayout+result.projectCounts.pieCounts.linesIncludingLayout),
            ofIntFunction("total lines excluding libraries, including layout" , result -> result.projectCounts.javaCounts.linesIncludingLayout+result.projectCounts.pieCounts.linesIncludingLayout-result.projectCounts.pieLibraryCounts.linesIncludingLayout),
            ofIntFunction("total lines including libraries, excluding layout" , result -> result.projectCounts.javaCounts.linesExcludingLayout+result.projectCounts.pieCounts.linesExcludingLayout),
            ofIntFunction("total lines excluding libraries and layout" , result -> result.projectCounts.javaCounts.linesExcludingLayout+result.projectCounts.pieCounts.linesExcludingLayout-result.projectCounts.pieLibraryCounts.linesExcludingLayout),
            // todo: also count lines for tasks only (i.e. src/main/java/mb.tiger.spoofax.task)
            new ResultFormatter.SeparatorRow(),
            ofIntFunction("java characters including layout", result -> result.projectCounts.javaCounts.charsIncludingLayout),
            ofIntFunction("java characters excluding layout", result -> result.projectCounts.javaCounts.charsExcludingLayout),
            ofIntFunction("PIE DSL characters including libraries and layout" , result -> result.projectCounts.pieCounts.charsIncludingLayout),
            ofIntFunction("PIE DSL characters excluding libraries, including layout" , result -> result.projectCounts.pieCounts.charsIncludingLayout-result.projectCounts.pieLibraryCounts.charsIncludingLayout),
            ofIntFunction("PIE DSL characters including libraries, excluding layout" , result -> result.projectCounts.pieCounts.charsExcludingLayout),
            ofIntFunction("PIE DSL characters excluding libraries and layout" , result -> result.projectCounts.pieCounts.charsExcludingLayout-result.projectCounts.pieLibraryCounts.charsExcludingLayout),
            ofIntFunction("total characters including libraries and layout" , result -> result.projectCounts.javaCounts.charsIncludingLayout+result.projectCounts.pieCounts.charsIncludingLayout),
            ofIntFunction("total characters excluding libraries, including layout" , result -> result.projectCounts.javaCounts.charsIncludingLayout+result.projectCounts.pieCounts.charsIncludingLayout-result.projectCounts.pieLibraryCounts.charsIncludingLayout),
            ofIntFunction("total characters including libraries, excluding layout" , result -> result.projectCounts.javaCounts.charsExcludingLayout+result.projectCounts.pieCounts.charsExcludingLayout),
            ofIntFunction("total characters excluding libraries and layout" , result -> result.projectCounts.javaCounts.charsExcludingLayout+result.projectCounts.pieCounts.charsExcludingLayout-result.projectCounts.pieLibraryCounts.charsExcludingLayout),
            // todo: also count characters for tasks only (i.e. src/main/java/mb.tiger.spoofax.task)
            new ResultFormatter.SeparatorRow(),
            ofIntFunction("tasks implemented in java" , result -> result.taskCounts.javaTasks),
            ofIntFunction("tasks fully implemented in PIE DSL" , result -> result.taskCounts.pieTasks-result.taskCounts.pieTasksWithHelperFunction),
            ofIntFunction("tasks implemented in PIE DSL with helper function" , result -> result.taskCounts.pieTasksWithHelperFunction),
            ofIntFunction("total tasks implemented in PIE DSL" , result -> result.taskCounts.pieTasks),
            ofIntFunction("total tasks" , result -> result.taskCounts.javaTasks+result.taskCounts.pieTasks)
            // todo: foreign java tasks in libraries
        );
    }


    static void appendPaddedString(StringBuilder sb, String str, int totalLength, boolean alignLeft) {
        if (alignLeft) {
            sb.append(str);
            appendSpaces(sb, totalLength - str.length());
        } else {
            appendSpaces(sb, totalLength - str.length());
            sb.append(str);
        }
    }

    static void appendSpaces(StringBuilder sb, int amount) {
        appendChars(sb, amount, ' ');
    }

    static void appendChars(StringBuilder sb, int amount, char chr) {
        for(int i = 0; i < amount; i++) {
            sb.append(chr);
        }
    }

    class Column {
        public final String header;
        public final int size;
        public final Function<RowProducer, String> cellBuilder;
        public final boolean alignLeft;

        public Column(String header, int size, Function<RowProducer, String> cellBuilder, boolean alignLeft) {
            if (header.length() > size) {
                throw new CellContentOutOfBoundsException(header, size, header);
            }
            this.header = header;
            this.size = size;
            this.cellBuilder = cellBuilder.andThen(contents -> {
                if (contents.length() > size) {
                    throw new CellContentOutOfBoundsException(header, size, contents);
                }
                return contents;
            });
            this.alignLeft = alignLeft;
        }

        public static String percentageDiff(RowProducer rowProducer,
                                            ProjectEvaluationResult baselineResult,
                                            ProjectEvaluationResult newResult) {
            final int baseline = rowProducer.getFunction.apply(baselineResult);
            final int newVal = rowProducer.getFunction.apply(newResult);
            double percentage = ((double)(newVal - baseline)) / (double)baseline * 100.0;
            if (baseline == 0) {
                if (newVal > 0) {
                    return "+inf %";
                } else if (newVal < 0) {
                    return "-inf %";
                }
                percentage = 0.0;
            }
            return String.format("%c%2.2f %%", percentage > 0.0 ? '+' : ' ' , percentage);
        }

        public static String absoluteDiff(RowProducer rowProducer, ProjectEvaluationResult newResult, ProjectEvaluationResult baselineResult) {
            final int baseline = rowProducer.getFunction.apply(baselineResult);
            final int newVal = rowProducer.getFunction.apply(newResult);
            return intToString(newVal - baseline, true);
        }

        public static String singleVal(RowProducer rowProducer, ProjectEvaluationResult result) {
            return intToString(rowProducer.getFunction.apply(result), false);
        }

        public void appendHeader(StringBuilder sb) {
            appendPaddedString(sb, header, size, alignLeft);
        }

        public void appendCell(StringBuilder sb, RowProducer rowProducer) {
            appendPaddedString(sb, cellBuilder.apply(rowProducer), size, alignLeft);
        }

        public static String intToString(int val, boolean addSign) {
            if (val == 0) {
                return addSign ? "0" : "-";
            } else if (val < 0) {
                if (addSign) {
                    return Integer.toString(val);
                }

                // Does not need to be an error, but I am not expecting it so probably signifies a bug
                throw new RuntimeException("Unexpected negative value "+val);
            }

            return (addSign ? "+" : "") + val;
        }

        public static class CellContentOutOfBoundsException extends RuntimeException {
            public CellContentOutOfBoundsException(String header, int size, String contents) {
                super(String.format("Cell contents '%s' are too large for column '%s' of size %d", contents, header, size));
            }
        }
    }

    interface Row { }

    class SeparatorRow implements Row { }

    class RowProducer implements Row {
        public final String name;
        public final Function<ProjectEvaluationResult, Integer> getFunction;

        private RowProducer(String name, Function<ProjectEvaluationResult, Integer> getFunction) {
            this.name = name;
            this.getFunction = getFunction;
        }

        public static RowProducer ofIntFunction(String name, Function<ProjectEvaluationResult, Integer> getFunction) {
            return new RowProducer(name, getFunction);
        }
    }
}
