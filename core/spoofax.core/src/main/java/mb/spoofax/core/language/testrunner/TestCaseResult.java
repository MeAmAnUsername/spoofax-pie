package mb.spoofax.core.language.testrunner;

import mb.common.message.KeyedMessages;
import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a testcase that is being run.
 *
 * Used by the Eclipse UI part of the runner. It notifies its parent (if any) when it gets the result for the test whose
 * run it represents.
 */
public class TestCaseResult implements Serializable {

    public final String description;
    public final ResourceKey file;
    public final Region descriptionRegion;

    public final KeyedMessages messages;

    public final long duration;

    /**
     * Create a TestCaseRun, representing a run of an ITestCase.
     * @param description
     *            description of the testcase
     * @param descriptionRegion
     *            region containing the description in the SPT file
     * @param file
     *            SPT file containing the testcase
     * @param messages
     *            messages generated by this testcase
     * @param duration
     *            time that passed during the execution of the testcase
     */
    public TestCaseResult(String description, Region descriptionRegion, ResourceKey file, KeyedMessages messages, long duration) {
        this.description = description;
        this.descriptionRegion = descriptionRegion;
        this.file = file;
        this.messages = messages;
        this.duration = duration;
    }

    public void addToStringBuilder(StringBuilder builder) {
        builder.append(this.description);
        if(!messages.containsError()) {
            builder.append(": PASS\n");
        } else {
            builder.append(": FAIL\n");
        }
        messages.addToStringBuilder(builder);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        TestCaseResult that = (TestCaseResult)o;
        return duration == that.duration && description.equals(that.description) && file.equals(that.file) && descriptionRegion.equals(that.descriptionRegion) && messages.equals(that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, file, descriptionRegion, messages, duration);
    }

    @Override
    public String toString() {
        return "TestCaseResult{description=" + description + ", file=" + file + ", duration=" + duration + "}";
    }
}
