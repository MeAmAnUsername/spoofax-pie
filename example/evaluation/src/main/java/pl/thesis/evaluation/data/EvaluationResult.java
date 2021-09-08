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

    @NonNull
    public String formatAsTable() {
        final int indentation = 2;

        StringBuilder sb = new StringBuilder();
        appendSpaces(sb, indentation);
        sb.append("| ");
        final String rowStart = sb.toString();
        final String separator = " | ";
        final String rowEnd = " |\n";

        final List<Column> columns = getColumns();
        sb = new StringBuilder("Evaluation result\n");

        // header
        sb.append(rowStart);
        AtomicBoolean first = new AtomicBoolean(true);
        for(Column column : columns) {
            if (!first.get()) {
                sb.append(separator);
            }
            column.appendHeader(sb);
            first.set(false);
        }
        sb.append(rowEnd);

        // rest of the table
        for(RowProducer rowProducer : getRowProducers()) {
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
        return sb.toString();
    }

    private static void appendPaddedString(StringBuilder sb, String str, int totalLength, boolean alignLeft) {
        if (alignLeft) {
            sb.append(str);
            appendSpaces(sb, totalLength - str.length());
        } else {
            appendSpaces(sb, totalLength - str.length());
            sb.append(str);
        }
    }

    private static void appendSpaces(StringBuilder sb, int amount) {
        for(int i = 0; i < amount; i++) {
            sb.append(' ');
        }
    }

    private List<Column> getColumns() {
        final Column[] columns = {
            new Column("value", 50, rowProducer -> rowProducer.name, true),
            new Column("Java", 5, rowProducer -> Column.intToString(rowProducer.getFunction.apply(javaResult), false), false),
            new Column("old PIE DSL", 11, rowProducer -> Column.intToString(rowProducer.getFunction.apply(oldPieResult), false), false),
            new Column("new PIE DSL", 11, rowProducer -> Column.intToString(rowProducer.getFunction.apply(newPieResult), false), false),
            new Column("absolute difference between Java and new PIE DSL", 48, rowProducer -> Column.intToString(rowProducer.getFunction.apply(newPieResult) - rowProducer.getFunction.apply(javaResult), true), false),
        };
        return Arrays.asList(columns);
    }

    private List<RowProducer> getRowProducers() {
        final RowProducer[] rowProducers = {
            RowProducer.ofIntFunction("java lines including layout", result -> result.projectCounts.javaCounts.linesIncludingLayout),
            RowProducer.ofIntFunction("java lines excluding layout", result -> result.projectCounts.javaCounts.linesExcludingLayout),
            RowProducer.ofIntFunction("PIE DSL lines including layout" , result -> result.projectCounts.pieCounts.linesIncludingLayout),
            RowProducer.ofIntFunction("PIE DSL lines excluding layout" , result -> result.projectCounts.pieCounts.linesExcludingLayout),
            RowProducer.ofIntFunction("total lines including layout" , result -> result.projectCounts.javaCounts.linesIncludingLayout+result.projectCounts.pieCounts.linesIncludingLayout),
            RowProducer.ofIntFunction("total lines excluding layout" , result -> result.projectCounts.javaCounts.linesExcludingLayout+result.projectCounts.pieCounts.linesExcludingLayout),
            RowProducer.ofIntFunction("java characters including layout", result -> result.projectCounts.javaCounts.charsIncludingLayout),
            RowProducer.ofIntFunction("java characters excluding layout", result -> result.projectCounts.javaCounts.charsExcludingLayout),
            RowProducer.ofIntFunction("PIE DSL characters including layout" , result -> result.projectCounts.pieCounts .charsIncludingLayout),
            RowProducer.ofIntFunction("PIE DSL characters excluding layout" , result -> result.projectCounts.pieCounts .charsExcludingLayout),
            RowProducer.ofIntFunction("total characters including layout" , result -> result.projectCounts.javaCounts.charsIncludingLayout+result.projectCounts.pieCounts.charsIncludingLayout),
            RowProducer.ofIntFunction("total characters excluding layout" , result -> result.projectCounts.javaCounts.charsExcludingLayout+result.projectCounts.pieCounts.charsExcludingLayout),
            RowProducer.ofIntFunction("tasks implemented in java" , result -> result.taskCounts.javaTasks),
            RowProducer.ofIntFunction("tasks fully implemented in PIE DSL" , result -> result.taskCounts.pieTasks-result.taskCounts.pieTasksWithHelperFunction),
            RowProducer.ofIntFunction("tasks implemented in PIE DSL with helper function" , result -> result.taskCounts.pieTasksWithHelperFunction),
            RowProducer.ofIntFunction("total tasks implemented in PIE DSL" , result -> result.taskCounts.pieTasks),
            RowProducer.ofIntFunction("total tasks" , result -> result.taskCounts.javaTasks+result.taskCounts.pieTasks),
        };
        return Arrays.asList(rowProducers);
    }

    private static class Column {
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

    private static class RowProducer {
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
