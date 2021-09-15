package pl.thesis.evaluation.tasks;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IsLibraryFileTest {
    public final String TASK = "module mb:tiger:spoofax:task";

    @Test
    public void emptyList() {
        Collection<String> ownModules = Collections.emptySet();
        assertTrue(IsLibraryFile.isLibraryFile(ownModules, TASK));
    }

    @Test
    public void fullMatch() {
        Collection<String> ownModules = Collections.singleton("mb:tiger:spoofax");
        assertFalse(IsLibraryFile.isLibraryFile(ownModules, TASK));
    }

    @Test
    public void partialMatch() {
        Collection<String> ownModules = Collections.singleton("mb:tiger");
        assertFalse(IsLibraryFile.isLibraryFile(ownModules, TASK));
    }

    @Test
    public void matchWithoutStart() {
        Collection<String> ownModules = Collections.singleton("tiger:task");
        assertTrue(IsLibraryFile.isLibraryFile(ownModules, TASK));
    }

    @Test
    public void noMatch() {
        Collection<String> ownModules = Collections.singleton("mb:tiger:task");
        assertTrue(IsLibraryFile.isLibraryFile(ownModules, TASK));
    }

    @Test
    public void multipleNoMatch() {
        Collection<String> ownModules = Arrays.asList("org:example:common", "mb:zebra:spoofax", "org:spoofax:stratego");
        assertTrue(IsLibraryFile.isLibraryFile(ownModules, TASK));
    }

    @Test
    public void multipleMatch() {
        Collection<String> ownModules = Arrays.asList("org:example:common", "mb:tiger:spoofax", "org:spoofax:stratego");
        assertFalse(IsLibraryFile.isLibraryFile(ownModules, TASK));
    }
}
