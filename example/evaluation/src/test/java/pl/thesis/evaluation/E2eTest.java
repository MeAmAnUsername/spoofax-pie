package pl.thesis.evaluation;

import mb.common.result.Result;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import pl.thesis.evaluation.data.EvaluationResult;
import pl.thesis.evaluation.data.FileCounts;
import pl.thesis.evaluation.data.ProjectCounts;
import pl.thesis.evaluation.data.Projects;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
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

        Collection<String> ownModules = Arrays.asList(
            Optional.ofNullable(props.getProperty("ownModules"))
                .map(s -> s.split(";"))
                .orElse(new String[0]));

        ProjectCounts expected = new ProjectCounts(
            new FileCounts(
                getIntProperty(props, "java.lines"),
                getIntProperty(props, "java.linesNoLayout"),
                getIntProperty(props, "java.characters"),
                getIntProperty(props, "java.charactersNoLayout")
            ),
            new FileCounts(
                getIntProperty(props, "pie.lines"),
                getIntProperty(props, "pie.linesNoLayout"),
                getIntProperty(props, "pie.characters"),
                getIntProperty(props, "pie.charactersNoLayout")
            ),
            new FileCounts(
                getIntProperty(props, "pieLibrary.lines"),
                getIntProperty(props, "pieLibrary.linesNoLayout"),
                getIntProperty(props, "pieLibrary.characters"),
                getIntProperty(props, "pieLibrary.charactersNoLayout")
            )
        );

        final Projects projects = new Projects(new FSPath(dir), new FSPath(dir), new FSPath(dir), ownModules);
        final FSPath resultFile = new FSPath(RESULT_FILE_DIR.resolve(dir.getFileName())).ensureLeafExtension("txt");

        @SuppressWarnings("NullableProblems") // Cannot find NonNull and error isn't used anyway, so just ignore
        Result<EvaluationResult, ?> result = Main.evaluateProject(projects, resultFile, null);
        ProjectCounts actual = result.unwrap().javaResult.projectCounts;
        assertEquals(expected, actual);
        assertTrue(Files.exists(resultFile.getJavaPath()));
    }

    private static int getIntProperty(Properties properties, String name) {
        return Integer.parseInt(properties.getProperty(name));
    }
}
