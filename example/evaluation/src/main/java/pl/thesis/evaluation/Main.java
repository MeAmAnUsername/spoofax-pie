package pl.thesis.evaluation;

import mb.common.result.Result;
import mb.pie.api.ExecException;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.tasks.CountCharacters;
import pl.thesis.evaluation.tasks.CountFileCharacters;
import pl.thesis.evaluation.tasks.CountFileLines;
import pl.thesis.evaluation.tasks.CountLines;
import pl.thesis.evaluation.tasks.EvaluateProject;
import pl.thesis.evaluation.tasks.ProjectEvaluationResult;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        CountFileLines countFileLines = new CountFileLines();
        CountLines countLines = new CountLines(countFileLines);
        CountFileCharacters countFileCharacters = new CountFileCharacters();
        CountCharacters countCharacters = new CountCharacters(countFileCharacters);
        EvaluateProject main = new EvaluateProject(countLines, countCharacters);

        Pie pie = new PieBuilderImpl()
            .addTaskDefs(new MapTaskDefs(countFileLines, countLines, countFileCharacters, countCharacters, main))
            .build();

        try(MixedSession session = pie.newSession()) {
            FSPath srcDir = new FSPath(Paths.get("src"));
            Result<@NonNull ProjectEvaluationResult, @NonNull Exception> res = session.require(main.createTask(srcDir));
            System.out.println("Done: " + res);
        } catch(ExecException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
