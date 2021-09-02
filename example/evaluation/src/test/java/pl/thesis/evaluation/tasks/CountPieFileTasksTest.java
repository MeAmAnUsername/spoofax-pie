package pl.thesis.evaluation.tasks;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountPieFileTasksTest {
    private final Path DIR = Paths.get("src", "test", "resources", "task-counts", "pie");

    @TestFactory
    public Stream<DynamicTest> testPieFiles() throws IOException {
        Properties properties = new Properties();
        final Path propFile = DIR.resolve("expected.properties");
        properties.load(new FileInputStream(propFile.toFile()));

        return DynamicTest.stream(
            Files.walk(DIR)
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".pie")),
            this::getFileNameWithoutExtension,
            file -> testPieFile(properties, file)
        );
    }

    private String getFileNameWithoutExtension(Path file) {
        final String fileName = file.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private void testPieFile(Properties properties, Path file) throws IOException {
        final String program = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        final int actual = CountPieFileTasks.countTasks(program);
        final int expected = Integer.parseInt(properties.getProperty(getFileNameWithoutExtension(file)));
        assertEquals(expected, actual);
    }
}
