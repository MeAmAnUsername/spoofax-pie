package pl.thesis.evaluation;

import mb.common.result.Result;
import mb.pie.api.ExecException;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
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
        TaskDefs taskDefs = buildTaskDefs();
        Pie pie = new PieBuilderImpl()
            .addTaskDefs(taskDefs)
            .build();
        EvaluateProject main = getTaskDef(taskDefs, EvaluateProject.class);

        try(MixedSession session = pie.newSession()) {
            return session.require(main.createTask(dir));
        } catch(ExecException | InterruptedException | NullPointerException e) {
            return Result.ofErr(e);
        }
    }

    public static TaskDefs buildTaskDefs() {
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

        return new MapTaskDefs(
            countFileLinesAndCharacters,
            countLinesAndCharacters,
            isTaskDef,
            countTaskDefs,
            countTasks,
            countPieFileTasks,
            countPieTasks,
            countPieFileTasksWithHelperFunction,
            countPieTasksWithHelperFunction,
            main);
    }

    public static <T extends TaskDef<@NonNull ?, @NonNull ?>> T getTaskDef(@NonNull TaskDefs taskDefs, Class<T> clazz) {
        final String id = clazz.getSimpleName();
        TaskDef<@NonNull ?, @NonNull ?> taskDef = taskDefs.getTaskDef(id);
        if (taskDef == null) {
            throw new NullPointerException("Could not get task '"+id+"'");
        }
        // noinspection unchecked  already checked if taskdef is assignable to clazz
        return (T) taskDef;
    }
}
