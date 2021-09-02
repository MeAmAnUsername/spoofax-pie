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
import pl.thesis.evaluation.tasks.CountPieFileTasks;
import pl.thesis.evaluation.tasks.CountPieFileTasksWithHelperFunction;
import pl.thesis.evaluation.tasks.CountPieTasks;
import pl.thesis.evaluation.tasks.CountPieTasksWithHelperFunction;
import pl.thesis.evaluation.tasks.CountTaskDefs;
import pl.thesis.evaluation.tasks.CountTasks;
import pl.thesis.evaluation.tasks.EvaluateProject;
import pl.thesis.evaluation.tasks.IsTaskDef;
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
        IsTaskDef isTaskDef = new IsTaskDef();
        CountTaskDefs countTaskDefs = new CountTaskDefs(isTaskDef);
        CountPieFileTasks countPieFileTasks = new CountPieFileTasks();
        CountPieTasks countPieTasks = new CountPieTasks(countPieFileTasks);
        CountPieFileTasksWithHelperFunction countPieFileTasksWithHelperFunction =
            new CountPieFileTasksWithHelperFunction();
        CountPieTasksWithHelperFunction countPieTasksWithHelperFunction =
            new CountPieTasksWithHelperFunction(countPieFileTasksWithHelperFunction);
        CountTasks countTasks = new CountTasks(countTaskDefs, countPieTasks, countPieTasksWithHelperFunction);
        EvaluateProject main = new EvaluateProject(countLinesAndCharacters, countTasks);

        Pie pie = new PieBuilderImpl()
            .addTaskDefs(new MapTaskDefs(
                countFileLinesAndCharacters,
                countLinesAndCharacters,
                isTaskDef,
                countTaskDefs,
                countTasks,
                countPieFileTasks,
                countPieTasks,
                countPieFileTasksWithHelperFunction,
                countPieTasksWithHelperFunction,
                main))
            .build();

        try(MixedSession session = pie.newSession()) {
            return session.require(main.createTask(dir));
        } catch(ExecException | InterruptedException e) {
            return Result.ofErr(e);
        }
    }
}
