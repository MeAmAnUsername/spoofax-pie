package pl.thesis.evaluation.tasks;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
        final int nameCellSize = 50;
        final int javaCellSize = 5;
        final int oldPieCellSize = 7;
        final int newPieCellSize = 7;

        StringBuilder sb = new StringBuilder("Evaluation result\n");
        appendSpaces(sb, indentation);
        sb.append("| ");
        appendPaddedStringAlignLeft(sb, "value", nameCellSize);
        sb.append(" | ");
        appendPaddedStringAlignLeft(sb, "java", javaCellSize);
        sb.append(" | ");
        appendPaddedStringAlignLeft(sb, "old PIE", oldPieCellSize);
        sb.append(" | ");
        appendPaddedStringAlignLeft(sb, "new PIE", newPieCellSize);
        sb.append(" |\n");
        for(RowProducer rowProducer : getRowProducers()) {
            appendSpaces(sb, indentation);
            sb.append("| ");
            sb.append(rowProducer.name);
            appendSpaces(sb, nameCellSize - rowProducer.name.length());
            sb.append(" | ");
            appendPaddedStringAlignRight(sb, rowProducer.getFunction.apply(javaResult), javaCellSize);
            sb.append(" | ");
            appendPaddedStringAlignRight(sb, rowProducer.getFunction.apply(oldPieResult), oldPieCellSize);
            sb.append(" | ");
            appendPaddedStringAlignRight(sb, rowProducer.getFunction.apply(newPieResult), newPieCellSize);
            sb.append(" |\n");
        }
        return sb.toString();
    }

    private void appendPaddedStringAlignLeft(StringBuilder sb, String str, int totalLength) {
        sb.append(str);
        appendSpaces(sb, totalLength - str.length());
    }

    private void appendPaddedStringAlignRight(StringBuilder sb, String str, int totalLength) {
        appendSpaces(sb, totalLength - str.length());
        sb.append(str);
    }

    private void appendSpaces(StringBuilder sb, int amount) {
        for(int i = 0; i < amount; i++) {
            sb.append(' ');
        }
    }

    private List<RowProducer> getRowProducers() {
        final RowProducer[] rowProducers = {
            RowProducer.ofIntFunction("java lines including layout", result -> result.projectCounts.javaCounts.linesIncludingLayout),
            RowProducer.ofIntFunction("java lines excluding layout", result -> result.projectCounts.javaCounts.linesIncludingLayout),
            RowProducer.ofIntFunction("PIE DSL lines including layout" , result -> result.projectCounts.pieCounts.linesIncludingLayout),
            RowProducer.ofIntFunction("PIE DSL lines excluding layout" , result -> result.projectCounts.pieCounts.linesIncludingLayout),
            RowProducer.ofIntFunction("total lines including layout" , result -> result.projectCounts.javaCounts.linesIncludingLayout+result.projectCounts.pieCounts.linesIncludingLayout),
            RowProducer.ofIntFunction("total lines excluding layout" , result -> result.projectCounts.javaCounts.linesIncludingLayout+result.projectCounts.pieCounts.linesIncludingLayout),
            RowProducer.ofIntFunction("java characters including layout", result -> result.projectCounts.javaCounts.charsIncludingLayout),
            RowProducer.ofIntFunction("java characters excluding layout", result -> result.projectCounts.javaCounts.charsIncludingLayout),
            RowProducer.ofIntFunction("PIE DSL characters including layout" , result -> result.projectCounts.pieCounts .charsIncludingLayout),
            RowProducer.ofIntFunction("PIE DSL characters excluding layout" , result -> result.projectCounts.pieCounts .charsIncludingLayout),
            RowProducer.ofIntFunction("total characters including layout" , result -> result.projectCounts.javaCounts.charsIncludingLayout+result.projectCounts.pieCounts.charsIncludingLayout),
            RowProducer.ofIntFunction("total characters excluding layout" , result -> result.projectCounts.javaCounts.charsIncludingLayout+result.projectCounts.pieCounts.charsIncludingLayout),
            RowProducer.ofIntFunction("tasks implemented in java" , result -> result.taskCounts.javaTasks),
            RowProducer.ofIntFunction("tasks fully implemented in PIE DSL" , result -> result.taskCounts.pieTasks-result.taskCounts.pieTasksWithHelperFunction),
            RowProducer.ofIntFunction("tasks implemented in PIE DSL with helper function" , result -> result.taskCounts.pieTasks-result.taskCounts.pieTasksWithHelperFunction),
            RowProducer.ofIntFunction("total tasks implemented in PIE DSL" , result -> result.taskCounts.pieTasks),
            RowProducer.ofIntFunction("total tasks" , result -> result.taskCounts.javaTasks+result.taskCounts.pieTasks),
        };
        return Arrays.asList(rowProducers);
    }

    private static class RowProducer {
        public final String name;
        public final Function<ProjectEvaluationResult, String> getFunction;

        private RowProducer(String name, Function<ProjectEvaluationResult, String> getFunction) {
            this.name = name;
            this.getFunction = getFunction;
        }

        public static RowProducer ofIntFunction(String name, Function<ProjectEvaluationResult, Integer> getFunction) {
            return new RowProducer(name, getFunction.andThen(RowProducer::intToStringDashForZero));
        }

        public static String intToStringDashForZero(int val) {
            if (val < 0) {
                // Does not need to be an error, but I am not expecting it so probably signifies a bug
                throw new RuntimeException("Unexpected negative value "+val);
            }

            return val == 0 ? "-" : Integer.toString(val);
        }
    }
}
