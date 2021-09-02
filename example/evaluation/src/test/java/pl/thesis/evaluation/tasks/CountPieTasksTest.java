package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.TaskDefs;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import org.junit.jupiter.api.Test;
import pl.thesis.evaluation.Main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountPieTasksTest {
    private final Path DIR = Paths.get("src", "test", "resources", "task-counts");

    @Test
    public void testAll() throws Exception {
        final TaskDefs taskDefs = Main.buildTaskDefs();
        final Pie pie = new PieBuilderImpl().addTaskDefs(taskDefs).build();
        final CountPieTasks countPieTasks = Main.getTaskDef(taskDefs, CountPieTasks.class);
        final ResourcePath input = new FSPath(DIR);

        try (MixedSession session = pie.newSession()) {
            final int actual = session.require(countPieTasks.createTask(input)).unwrap();
            final int expected = 22;
            assertEquals(expected, actual);
        }
    }
}
