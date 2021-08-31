package pl.thesis.evaluation;

import mb.common.result.Result;
import mb.pie.api.ExecException;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.tasks.CountLinesAndCharacters;
import pl.thesis.evaluation.tasks.CountFileLinesAndCharacters;
import pl.thesis.evaluation.tasks.EvaluateProject;
import pl.thesis.evaluation.tasks.ProjectEvaluationResult;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        FSPath dir = new FSPath(Paths.get("..", "tiger", "manual", "tiger.spoofax", "src", "main"));
        final Result<@NonNull ProjectEvaluationResult, @NonNull Exception> evaluationResult = evaluateProject(dir);
        System.out.println("Done: " + evaluationResult);
    }

    public static Result<@NonNull ProjectEvaluationResult, @NonNull Exception> evaluateProject(FSPath dir) {
        CountFileLinesAndCharacters countFileLinesAndCharacters = new CountFileLinesAndCharacters();
        CountLinesAndCharacters countLinesAndCharacters = new CountLinesAndCharacters(countFileLinesAndCharacters);
        EvaluateProject main = new EvaluateProject(countLinesAndCharacters);

        Pie pie = new PieBuilderImpl()
            .addTaskDefs(new MapTaskDefs(countFileLinesAndCharacters, countLinesAndCharacters, main))
            .build();

        try(MixedSession session = pie.newSession()) {
            return session.require(main.createTask(dir));
        } catch(ExecException | InterruptedException e) {
            return Result.ofErr(e);
        }
    }
}
