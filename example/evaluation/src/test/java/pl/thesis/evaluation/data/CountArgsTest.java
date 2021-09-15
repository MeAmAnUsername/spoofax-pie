package pl.thesis.evaluation.data;

import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CountArgsTest {
    public static final ResourcePath PATH = new FSPath(Paths.get("."));

    @Test
    public void emptySet() {
        create(Collections.emptySet());
    }

    @Test
    public void emptyName() {
        assertFails("");
    }

    @Test
    public void illegalStart() {
        assertFails("0test");
    }

    @Test
    public void illegalMiddle() {
        assertFails("mb:-common:spoofax");
    }

    @Test
    public void periodsAsSeparator() {
        assertFails("mb.tiger.spoofax");
    }

    @Test
    public void emptyMiddleName() {
        assertFails("mb::spoofax");
    }

    @Test
    public void emptyStartName() {
        assertFails(":lion:spoofax");
    }

    @Test
    public void emptyEndName() {
        assertFails("org:zebra:");
    }

    @Test
    public void correct() {
        create(Collections.singleton("mb:tiger:spoofax"));
    }

    @Test
    public void multipleNamesCorrect() {
        create(Arrays.asList("mb:tiger", "org:metaborg:common", "java:util"));
    }

    @Test
    public void multipleNamesIncorrect() {
        assertFails(Arrays.asList("mb:tiger", "org:-metaborg:common", "java:util"));
    }

    private void assertFails(String moduleName) {
        assertFails(Collections.singleton(moduleName));
    }

    private void assertFails(Collection<String> moduleNames) {
        assertThrows(IllegalArgumentException.class, () -> create(moduleNames));
    }

    private void create(Collection<String> moduleNames) {
        new CountArgs(PATH, moduleNames);
    }
}
