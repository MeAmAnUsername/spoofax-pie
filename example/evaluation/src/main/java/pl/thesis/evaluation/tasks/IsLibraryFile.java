package pl.thesis.evaluation.tasks;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

public class IsLibraryFile implements TaskDef<IsLibraryFile.Input, @NonNull Boolean> {
    public static class Input implements Serializable {
        public final @NonNull Supplier<@NonNull String> contentsSupplier;
        public final @NonNull Collection<@NonNull String> ownModules;

        public Input(@NonNull Supplier<String> contentsSupplier, @NonNull Collection<String> ownModules) {
            this.contentsSupplier = contentsSupplier;
            this.ownModules = ownModules;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return contentsSupplier.equals(input.contentsSupplier) && ownModules.equals(input.ownModules);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contentsSupplier, ownModules);
        }

        @Override public String toString() {
            return "Input{" +
                "contentsSupplier=" + contentsSupplier +
                ", ownModules=" + ownModules +
                '}';
        }
    }

    @Override
    public @NonNull String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public @NonNull Boolean exec(@NonNull ExecContext context, @NonNull Input input) {
        final String contents = context.require(input.contentsSupplier);
        assert contents != null;
        return isLibraryFile(input.ownModules, contents);
    }

    public static boolean isLibraryFile(Collection<String> ownModules, String contents) {
        return ownModules.stream()
            .map(name -> "module " + name)
            .noneMatch(contents::contains);
    }
}
