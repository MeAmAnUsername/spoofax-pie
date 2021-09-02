package pl.thesis.evaluation.tasks;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountPieFileTasksTest {
    private final Path TIGER_PIE_FILE = Paths.get("src", "test", "resources", "tiger.pie");

    @Test
    public void noFuncs() {
        assertProgramHasTaskCount(0, "module test:count:func:none");
    }

    @Test
    public void foreignFunc() {
        assertProgramHasTaskCount(0, "module test:count:func:foreign\n\n" +
            "func external() -> unit =\n" +
            "  foreign External\n");
    }

    @Test
    public void dataNamedFunc() {
        assertProgramHasTaskCount(0, "module test:count:func:data\n\n" +
            "data func = foreign java Func {\n" +
            "  func get() -> int\n" +
            "  func set(int) -> unit\n" +
            "}\n");
    }

    @Test
    public void oneFunc() {
        assertProgramHasTaskCount(1, "module test:count:func:one\n\n" +
            "func somefunc() -> Result<int, _ : Exception> = okResult(0)\n");
    }

    @Test
    public void oneFuncWithBody() {
        assertProgramHasTaskCount(1, "module test:count:func:one\n\n" +
            "func somefunc() -> bool = {\n" +
            "  val func = unit;\n" +
            "  val foo: int* = 8;\n" +
            "}\n");
    }

    @Test
    public void multipleFuncs() {
        assertProgramHasTaskCount(3, "module test:count:func:multiple\n\n" +
            "func funcOne() -> Result<int, _ : Exception> = okResult(0)\n" +
            "func funcTwo<E>(e: Optional<E>) -> bool = false\n" +
            "func funcThree()[runtime: StrategoRuntime] -> bool = false\n" +
            "func funcFour() -> string = foreign java org:example:Foo#getString");
    }

    @Test
    public void tigerPieFile() throws IOException {
        assertProgramHasTaskCount(16, new String(Files.readAllBytes(TIGER_PIE_FILE), StandardCharsets.UTF_8));
    }

    public void assertProgramHasTaskCount(int expectedTaskCount, String program) {
        final int actual = CountPieFileTasks.countTasks(program);
        assertEquals(expectedTaskCount, actual);
    }
}
