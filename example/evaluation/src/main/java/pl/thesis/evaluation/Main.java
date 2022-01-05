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
import pl.thesis.evaluation.data.OutputFile;
import pl.thesis.evaluation.formatter.LatexOverviewTableResultFormatter;
import pl.thesis.evaluation.formatter.LatexTableResultFormatter;
import pl.thesis.evaluation.formatter.TextTableResultFormatter;
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
import pl.thesis.evaluation.tasks.IsLibraryFile;
import pl.thesis.evaluation.tasks.IsTaskDef;
import pl.thesis.evaluation.data.Projects;
import pl.thesis.evaluation.tasks.WriteEvaluationResultToFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        FSPath javaDir = new FSPath(Paths.get("..", "tiger", "manual", "tiger.spoofax", "src", "main"));
        FSPath oldPieDir = new FSPath(Paths.get("..", "tiger", "manual", "tiger.oldpie.spoofax", "src", "main"));
        FSPath newPieDir = new FSPath(Paths.get("..", "tiger", "manual", "tiger.newpie.spoofax", "src", "main"));
        FSPath resultFile = new FSPath(Paths.get("build", "reports", "case_study_evaluation.txt"));

        Properties properties = new Properties();
        Path propFile = Paths.get("config/config.properties");
        try {
            properties.load(Files.newInputStream(propFile));
        } catch(IOException e) {
            System.err.println("Could not create properties from " + propFile + ": " + e);
            e.printStackTrace();
        }
        FSPath latexFile = new FSPath(properties.getProperty("resultFiles.latex"));
        FSPath latexOverviewFile = new FSPath(properties.getProperty("resultFiles.latexOverview"));

        Collection<String> ownModules = Collections.singleton("mb:tiger:spoofax");

        final Projects projects = new Projects(javaDir, oldPieDir, newPieDir, ownModules);
        final List<OutputFile> outputFiles = Arrays.asList(
            new OutputFile(resultFile, new TextTableResultFormatter()),
            new OutputFile(latexFile, new LatexTableResultFormatter()),
            new OutputFile(latexOverviewFile, new LatexOverviewTableResultFormatter())
        );
        final Result<@NonNull EvaluationResult, @NonNull Exception> evaluationResult =
            evaluateProject(projects, outputFiles);
        System.out.println("Done: " + evaluationResult);
        if (evaluationResult.isOk()) {
            //noinspection ConstantConditions
            System.out.println(new TextTableResultFormatter().format(evaluationResult.get()));
        }
    }

    public static Result<@NonNull EvaluationResult, @NonNull Exception> evaluateProject(
        @NonNull Projects projects,
        Iterable<OutputFile> outputFiles
    ) {
        ResourceService resourceService = buildResourceService();
        TaskDefs taskDefs = buildTaskDefs(resourceService);
        Pie pie = new PieBuilderImpl()
            .addTaskDefs(taskDefs)
            .withResourceService(resourceService)
            .build();
        EvaluateCaseStudy evaluate = getTaskDef(taskDefs, EvaluateCaseStudy.class);

        try(MixedSession session = pie.newSession()) {
            final Result<@NonNull EvaluationResult, @NonNull Exception> result = session.require(evaluate.createTask(projects));
            assert result != null;
            if(result.isOk()) {
                WriteEvaluationResultToFile write = getTaskDef(taskDefs, WriteEvaluationResultToFile.class);
                for (OutputFile outputFile : outputFiles) {
                    //noinspection ConstantConditions
                    session.require(write.createTask(new WriteEvaluationResultToFile.Input(outputFile.formatter, outputFile.file, result.get())));
                }
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
        IsLibraryFile isLibraryFile = new IsLibraryFile();
        CountLinesAndCharacters countLinesAndCharacters = new CountLinesAndCharacters(countFileLinesAndCharacters, isLibraryFile);
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
            isLibraryFile,
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
