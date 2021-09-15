package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ModifiedResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import org.checkerframework.checker.nullness.qual.NonNull;
import pl.thesis.evaluation.data.CountArgs;
import pl.thesis.evaluation.data.FileCounts;
import pl.thesis.evaluation.data.ProjectCounts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CountLinesAndCharacters implements TaskDef<@NonNull CountArgs, @NonNull Result<@NonNull ProjectCounts, @NonNull Exception>> {

    @NonNull private final CountFileLinesAndCharacters countFileLinesAndCharacters;
    @NonNull private final IsLibraryFile isLibraryFile;

    public CountLinesAndCharacters(@NonNull CountFileLinesAndCharacters countFileLinesAndCharacters, @NonNull IsLibraryFile isLibraryFile) {
        this.countFileLinesAndCharacters = countFileLinesAndCharacters;
        this.isLibraryFile = isLibraryFile;
    }

    @java.lang.Override
    public @NonNull String getId() {
        return getClass().getSimpleName();
    }

    @java.lang.Override
    public @NonNull Result<@NonNull ProjectCounts, @NonNull Exception> exec(ExecContext context, @NonNull CountArgs input) {
        try {
            MutableFileCounts javaCounts = new MutableFileCounts();
            MutableFileCounts pieCounts = new MutableFileCounts();
            MutableFileCounts pieLibraryCounts = new MutableFileCounts();
            List<Exception> errors = new ArrayList<>();
            HierarchicalResource dir = context.require(input.dir);
            dir.walkForEach(ResourceMatcher.ofDirectory(), context::require);
            dir.walk(ResourceMatcher.ofFileExtension("java"))
                .map(file -> context.require(countFileLinesAndCharacters.createTask(file.getPath())))
                .forEach(result -> result.ifElse(javaCounts::addFileCounts, errors::add));
            dir.walk(ResourceMatcher.ofFileExtension("pie"))
                .forEach(file -> {
                    final Result<FileCounts, @NonNull Exception> result =
                        context.require(countFileLinesAndCharacters.createTask(file.getPath()));
                    result.ifElse(counts -> {
                        pieCounts.addFileCounts(counts);
                        final ResourceStringSupplier stringSupplier = new ResourceStringSupplier(file.getKey(),
                            new ModifiedResourceStamper<@NonNull ReadableResource>(), StandardCharsets.UTF_8);
                        if (context.require(isLibraryFile.createTask(new IsLibraryFile.Input(stringSupplier, input.ownModules)))) {
                            pieLibraryCounts.addFileCounts(counts);
                        }
                    }, errors::add);
                });
            if (!errors.isEmpty()) {
                return Result.ofErr(new CompositeException(errors));
            }
            return Result.ofOk(new ProjectCounts(javaCounts.toFileCounts(), pieCounts.toFileCounts(), pieLibraryCounts.toFileCounts()));
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }

    private static class MutableFileCounts {
        public int linesIncludingLayout;
        public int linesExcludingLayout;
        public int charsIncludingLayout;
        public int charsExcludingLayout;

        public void addFileCounts(FileCounts fileCounts) {
            linesIncludingLayout += fileCounts.linesIncludingLayout;
            linesExcludingLayout += fileCounts.linesExcludingLayout;
            charsIncludingLayout += fileCounts.charsIncludingLayout;
            charsExcludingLayout += fileCounts.charsExcludingLayout;
        }

        public FileCounts toFileCounts() {
            return new FileCounts(linesIncludingLayout,
                linesExcludingLayout,
                charsIncludingLayout,
                charsExcludingLayout);
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            MutableFileCounts that = (MutableFileCounts)o;
            return linesIncludingLayout == that.linesIncludingLayout && linesExcludingLayout == that.linesExcludingLayout && charsIncludingLayout == that.charsIncludingLayout && charsExcludingLayout == that.charsExcludingLayout;
        }

        @Override
        public int hashCode() {
            return Objects.hash(linesIncludingLayout, linesExcludingLayout, charsIncludingLayout, charsExcludingLayout);
        }

        @Override public String toString() {
            return "MutableFileCounts{" +
                "linesIncludingLayout=" + linesIncludingLayout +
                ", linesExcludingLayout=" + linesExcludingLayout +
                ", charsIncludingLayout=" + charsIncludingLayout +
                ", charsExcludingLayout=" + charsExcludingLayout +
                '}';
        }
    }

}
