package pl.thesis.evaluation.tasks;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class CompositeException extends Exception {
    @NonNull private final List<? extends Exception> errors;

    public CompositeException(@NonNull List<? extends Exception> errors) {
        this.errors = errors;
    }

    @Override public String toString() {
        return "CompositeException{" +
            "errors=" + errors.stream().map(Object::toString).collect(Collectors.joining(", ")) +
            '}';
    }
}
