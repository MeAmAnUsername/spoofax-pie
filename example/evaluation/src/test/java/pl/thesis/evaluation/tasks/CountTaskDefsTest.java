package pl.thesis.evaluation.tasks;

import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountTaskDefsTest {
    private final Path DIR = Paths.get("src", "test", "resources", "task-counts");

    @Test
    public void countAll() throws Exception {
        IsTaskDef isTaskDef = new IsTaskDef();
        CountTaskDefs countTaskDefs = new CountTaskDefs(isTaskDef);

        Pie pie = new PieBuilderImpl().addTaskDefs(new MapTaskDefs(isTaskDef, countTaskDefs)).build();
        try (MixedSession session = pie.newSession()) {
            final int actual = session.require(countTaskDefs.createTask(new FSPath(DIR))).unwrap();
            final int expected = 3;
            assertEquals(expected, actual);
        }
    }
}
