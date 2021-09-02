package pl.thesis.evaluation.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountPieFileTasksTest {
    @Test
    public void noFuncs() {
        final String program = "module test:count:func:none";
        final int actual = CountPieFileTasks.countTasks(program);
        final int expected = 0;
        assertEquals(expected, actual);
    }

    @Test
    public void foreignFunc() {
        final String program = "module test:count:func:foreign\n\n" +
            "func external() -> unit =\n" +
            "  foreign External\n";
        final int actual = CountPieFileTasks.countTasks(program);
        final int expected = 0;
        assertEquals(expected, actual);
    }

    @Test
    public void dataNamedFunc() {
        final String program = "module test:count:func:data\n\n" +
            "data func = foreign java Func {\n" +
            "  func get() -> int\n" +
            "  func set(int) -> unit\n" +
            "}\n";
        final int actual = CountPieFileTasks.countTasks(program);
        final int expected = 0;
        assertEquals(expected, actual);
    }

    @Test
    public void oneFunc() {
        final String program = "module test:count:func:one\n\n" +
            "func somefunc() -> Result<int, _ : Exception> = okResult(0)\n";
        final int actual = CountPieFileTasks.countTasks(program);
        final int expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void oneFuncWithBody() {
        final String program = "module test:count:func:one\n\n" +
            "func somefunc() -> bool = {\n" +
            "  val func = unit;\n" +
            "  val foo: int* = 8;\n" +
            "}\n";
        final int actual = CountPieFileTasks.countTasks(program);
        final int expected = 1;
        assertEquals(expected, actual);
    }

    @Test
    public void multipleFuncs() {
        final String program = "module test:count:func:multiple\n\n" +
            "func funcOne() -> Result<int, _ : Exception> = okResult(0)\n" +
            "func funcTwo<E>(e: Optional<E>) -> bool = false\n" +
            "func funcThree()[runtime: StrategoRuntime] -> bool = false\n" +
            "func funcFour() -> string = foreign java org:example:Foo#getString";
        final int actual = CountPieFileTasks.countTasks(program);
        final int expected = 3;
        assertEquals(expected, actual);
    }
}
