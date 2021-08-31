package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import pl.thesis.evaluation.Main;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountFileLinesAndCharactersTest {
    public final Path CASES_DIR = Paths.get("src", "test", "resources");

    @TestFactory
    public Stream<DynamicTest> createTests() throws IOException {
        return DynamicTest.stream(
            Files.list(CASES_DIR),
            dir -> dir.getName(dir.getNameCount() - 1).toString(),
            this::evaluateTest
        );
    }

    private void evaluateTest(Path dir) throws Exception {
        Properties props = new Properties();
        final Path propFile = dir.resolve("Empty.properties");
        props.load(new FileInputStream(propFile.toFile()));
        FileCounts expected = new FileCounts(
            getIntProperty(props, "lines"),
            getIntProperty(props, "linesNoLayout"),
            getIntProperty(props, "characters"),
            getIntProperty(props, "charactersNoLayout")
        );

        @SuppressWarnings("NullableProblems") // Cannot find NonNull and error isn't used anyway, so just ignore
        Result<ProjectEvaluationResult, ?> result = Main.evaluateProject(new FSPath(dir));
        FileCounts actual = result.unwrap().projectCounts.javaCounts;
        assertEquals(expected, actual);
    }

    private static int getIntProperty(Properties properties, String name) {
        return Integer.parseInt(properties.getProperty(name));
    }
}
