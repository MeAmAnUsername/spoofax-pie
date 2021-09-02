package pl.thesis.evaluation.tasks;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IsTaskDefTest {
    public final Path DIR = Paths.get("src", "test", "resources", "task-counts");

    @Test
    public void taskAIsTaskDef() throws IOException {
        checkFile(Paths.get("tasks", "taskA.java"), true);
    }

    @Test
    public void taskBIsTaskDef() throws IOException {
        checkFile(Paths.get("tasks", "taskB.java"), true);
    }

    @Test
    public void notTaskIsTaskDef() throws IOException {
        checkFile(Paths.get("tasks", "NotTask.java"), false);
    }

    @Test
    public void outsideIsTaskDef() throws IOException {
        checkFile(Paths.get("Outside.java"), true);
    }

    public void checkFile(Path relativePath, boolean expected) throws IOException {
        final Path file = DIR.resolve(relativePath);
        final String contents = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        assertEquals(expected, IsTaskDef.isTaskDef(contents));
    }
}
