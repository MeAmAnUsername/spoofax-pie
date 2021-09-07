package pl.thesis.evaluation.tasks;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import pl.thesis.evaluation.data.FileCounts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountFileLinesAndCharactersTest {
    public final Path UNIT_TESTS_FILE =
        Paths.get("src", "test", "resources", "CountFileLinesAndCharactersTestCases.txt");

    @TestFactory
    public Stream<DynamicTest> readUnitTests() throws IOException {
        return Files.lines(UNIT_TESTS_FILE)
            .skip(1) // skip the header
            .filter(str -> !"".equals(str))
            .map(this::parseLine);
    }

    private DynamicTest parseLine(String s) {
        final String[] split = s.split(" \\| ", 4);
        String testName = split[0].trim();
        int linesExcludingLayout = Integer.parseInt(split[1].trim());
        int charsExcludingLayout = Integer.parseInt(split[2].trim());
        String program = split[3]; // not trimmed
        return DynamicTest.dynamicTest(testName, () -> testFromString(program, linesExcludingLayout, charsExcludingLayout));
    }

    public void testFromString(String program, int linesExcludingLayout, int charactersExcludingLayout) throws IOException {
        final int linesIncludingLayout = (int) (program.chars().filter(chr -> chr == '\n').count()) + 1;
        final FileCounts expected = new FileCounts(linesIncludingLayout, linesExcludingLayout, program.length(), charactersExcludingLayout);
        final InputStream stream = new ByteArrayInputStream(program.getBytes());
        final FileCounts actual = new CountFileLinesAndCharacters().evaluateFile(stream);
        assertEquals(expected, actual);
    }
}
