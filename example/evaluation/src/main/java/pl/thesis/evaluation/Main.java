package pl.thesis.evaluation;

import mb.common.result.Result;
import mb.pie.api.ExecException;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import pl.thesis.evaluation.tasks.CountLinesAndCharacters;
import pl.thesis.evaluation.tasks.CountFileLinesAndCharacters;
import pl.thesis.evaluation.tasks.CountPieFileTasks;
import pl.thesis.evaluation.tasks.CountPieFileTasksWithHelperFunction;
import pl.thesis.evaluation.tasks.CountPieTasks;
import pl.thesis.evaluation.tasks.CountPieTasksWithHelperFunction;
import pl.thesis.evaluation.tasks.CountTaskDefs;
import pl.thesis.evaluation.tasks.CountTasks;
import pl.thesis.evaluation.tasks.EvaluateCaseStudy;
import pl.thesis.evaluation.tasks.EvaluateProject;
import pl.thesis.evaluation.data.EvaluationResult;
import pl.thesis.evaluation.tasks.IsTaskDef;
import pl.thesis.evaluation.data.ProjectDirs;
import pl.thesis.evaluation.tasks.WriteEvaluationResultToFile;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        FSPath javaDir = new FSPath(Paths.get("..", "tiger", "manual", "tiger.spoofax", "src", "main"));
        FSPath newPieDir = new FSPath(Paths.get("..", "tiger", "manual", "tiger.newpie.spoofax", "src", "main"));
        FSPath resultFile = new FSPath(Paths.get("build", "reports", "case_study_evaluation.txt"));

        final ProjectDirs projects = new ProjectDirs(javaDir, javaDir, newPieDir);
        final Result<@NonNull EvaluationResult, @NonNull Exception> evaluationResult = evaluateProject(projects, resultFile);
        System.out.println("Done: " + evaluationResult);
        if (evaluationResult.isOk()) {
            //noinspection ConstantConditions
            System.out.println(evaluationResult.get().formatAsTable());
        }
    }

    public static Result<@NonNull EvaluationResult, @NonNull Exception> evaluateProject(
            @NonNull ProjectDirs projects, @Nullable ResourcePath resultFile) {
        ResourceService resourceService = buildResourceService();
        TaskDefs taskDefs = buildTaskDefs(resourceService);
        Pie pie = new PieBuilderImpl()
            .addTaskDefs(taskDefs)
            .withResourceService(resourceService)
            .build();
        EvaluateCaseStudy evaluate = getTaskDef(taskDefs, EvaluateCaseStudy.class);

        try(MixedSession session = pie.newSession()) {
            final Result<@NonNull EvaluationResult, @NonNull Exception> result = session.require(evaluate.createTask(projects));
            if (resultFile != null && result.isOk()) {
                WriteEvaluationResultToFile write = getTaskDef(taskDefs, WriteEvaluationResultToFile.class);
                //noinspection ConstantConditions
                session.require(write.createTask(new WriteEvaluationResultToFile.Input(result.get(), resultFile)));
            }
            return result;
        } catch(ExecException | InterruptedException | NullPointerException e) {
            return Result.ofErr(e);
        }
    }

    public static ResourceService buildResourceService() {
        ResourceRegistry registry = new FSResourceRegistry();
        return new DefaultResourceService(registry);
    }

    public static TaskDefs buildTaskDefs(ResourceService resourceService) {
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
        EvaluateProject evaluateProject = new EvaluateProject(countLinesAndCharacters, countTasks);
        EvaluateCaseStudy evaluateCaseStudy = new EvaluateCaseStudy(evaluateProject);
        WriteEvaluationResultToFile writeEvaluationResultToFile = new WriteEvaluationResultToFile(resourceService);

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
            evaluateProject,
            evaluateCaseStudy,
            writeEvaluationResultToFile);
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
