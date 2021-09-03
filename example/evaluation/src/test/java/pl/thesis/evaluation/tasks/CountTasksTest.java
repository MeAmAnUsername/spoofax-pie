package pl.thesis.evaluation.tasks;

import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.TaskDefs;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import org.junit.jupiter.api.Test;
import pl.thesis.evaluation.Main;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountTasksTest {
    private final Path DIR = Paths.get("src", "test", "resources", "task-counts");

    @Test
    public void testAll() throws Exception {
        final TaskDefs taskDefs = Main.buildTaskDefs(Main.buildResourceService());
        final Pie pie = new PieBuilderImpl().addTaskDefs(taskDefs).build();
        final CountTasks main = Main.getTaskDef(taskDefs, CountTasks.class);
        final ResourcePath input = new FSPath(DIR);

        try (MixedSession session = pie.newSession()) {
            final TaskCounts actual = session.require(main.createTask(input)).unwrap();
            final TaskCounts expected = new TaskCounts(3, 22, 10);
            assertEquals(expected, actual);
        }
    }
}
