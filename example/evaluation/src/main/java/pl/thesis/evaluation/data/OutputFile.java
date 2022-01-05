package pl.thesis.evaluation.data;

import mb.resource.hierarchical.ResourcePath;
import pl.thesis.evaluation.formatter.ResultFormatter;

import java.util.Objects;

public class OutputFile {
    public final ResourcePath file;
    public final ResultFormatter formatter;

    public OutputFile(ResourcePath file, ResultFormatter formatter) {
        this.file = file;
        this.formatter = formatter;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        OutputFile that = (OutputFile)o;
        return Objects.equals(file, that.file) && Objects.equals(formatter, that.formatter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, formatter);
    }

    @Override public String toString() {
        return "OutputFile{" +
            "file=" + file +
            ", formatter=" + formatter +
            '}';
    }
}
