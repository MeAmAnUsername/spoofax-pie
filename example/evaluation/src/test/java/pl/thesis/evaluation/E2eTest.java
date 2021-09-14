package pl.thesis.evaluation;

import mb.common.result.Result;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import pl.thesis.evaluation.data.EvaluationResult;
import pl.thesis.evaluation.data.FileCounts;
import pl.thesis.evaluation.data.Projects;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2eTest {
    public final Path E2E_PROJECTS_DIR = Paths.get("src", "test", "resources", "e2e-projects");
    public final Path RESULT_FILE_DIR = Paths.get("build", "reports", "e2e-tests");

    @TestFactory
    public Stream<DynamicTest> createTests() throws IOException {
        return DynamicTest.stream(
            Files.list(E2E_PROJECTS_DIR),
            dir -> dir.getName(dir.getNameCount() - 1).toString(),
            this::evaluateTest
        );
    }

    private void evaluateTest(Path dir) throws Exception {
        Properties props = new Properties();
        final Path propFile = dir.resolve("testCase.properties");
        props.load(new FileInputStream(propFile.toFile()));
        FileCounts expected = new FileCounts(
            getIntProperty(props, "lines"),
            getIntProperty(props, "linesNoLayout"),
            getIntProperty(props, "characters"),
            getIntProperty(props, "charactersNoLayout")
        );

        final Projects projects = new Projects(new FSPath(dir), new FSPath(dir), new FSPath(dir), Collections.emptyList());
        final FSPath resultFile = new FSPath(RESULT_FILE_DIR.resolve(dir.getFileName())).ensureLeafExtension("txt");

        @SuppressWarnings("NullableProblems") // Cannot find NonNull and error isn't used anyway, so just ignore
        Result<EvaluationResult, ?> result = Main.evaluateProject(projects, resultFile);
        FileCounts actual = result.unwrap().javaResult.projectCounts.javaCounts;
        assertEquals(expected, actual);
        assertTrue(Files.exists(resultFile.getJavaPath()));
    }

    private static int getIntProperty(Properties properties, String name) {
        return Integer.parseInt(properties.getProperty(name));
    }
}
