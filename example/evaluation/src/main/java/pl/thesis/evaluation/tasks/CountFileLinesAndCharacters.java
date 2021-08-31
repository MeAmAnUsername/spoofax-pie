package pl.thesis.evaluation.tasks;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ModifiedResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Counts the lines and characters of a file, both including and excluding layout
 * Lines including layout count all lines of the file, which is the number of
 * newline characters+1.
 * Lines excluding layout do not count empty lines. A trailing newline is
 * counted if the last character on the line before it is not layout.
 * Characters including layout counts all characters of the file, including
 * trailing newlines.
 * Characters excluding newlines counts consecutive layout (whitespace and
 * comments) as a single character. A trailing newline is counted if the last
 * character on the preceding line is not layout.
 */
public class CountFileLinesAndCharacters implements TaskDef<@NonNull ResourcePath, @NonNull Result<FileCounts, @NonNull Exception>> {
    private enum ParseState {
        REGULAR,
        STRING,
        SINGLE_LINE_COMMENT,
        MULTI_LINE_COMMENT;

        public boolean isComment() {
            return this == SINGLE_LINE_COMMENT || this == MULTI_LINE_COMMENT;
        }
    }

    private enum OptionalBoolean {
        FALSE,
        MAYBE,
        TRUE
    }

    @Override
    @NonNull
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    @NonNull
    public Result<FileCounts, @NonNull Exception> exec(@NonNull ExecContext context, @NonNull ResourcePath input) {
        try {
            final ReadableResource file = context.require(input, new ModifiedResourceStamper<@NonNull ReadableResource>());
            try(InputStream inputStream = file.openRead()) {
                return Result.ofOk(evaluateFile(inputStream));
            }
        } catch(IOException | IllegalArgumentException e) {
            return Result.ofErr(e);
        }
    }

    @NonNull
    public FileCounts evaluateFile(InputStream stream) throws IOException {
        int lineCount = 1;
        int lineCountExcludingLayout = 0;
        OptionalBoolean lineHasRegularCharacters = OptionalBoolean.FALSE;
        int charCount = 0;
        int charCountExcludingLayout = 0;
        ParseState parseState = ParseState.REGULAR;
        int prevChar = 'a';
        ParseState previousParseState = ParseState.REGULAR;
        int chr;
        while((chr = stream.read()) != -1) {
            charCount++;
            final ParseState curState = parseState;
            switch(parseState) {
                case REGULAR:
                    switch(chr) {
                        case '"':
                            charCountExcludingLayout++;
                            lineHasRegularCharacters = OptionalBoolean.TRUE;
                            parseState = ParseState.STRING;
                            break;
                        case '/':
                            if(prevChar == '/') {
                                // decrement because previous '/' is layout
                                charCountExcludingLayout--;
                                parseState = ParseState.SINGLE_LINE_COMMENT;
                            } else {
                                // increment in case this is not the start of a comment
                                charCountExcludingLayout++;
                                if(lineHasRegularCharacters == OptionalBoolean.FALSE) {
                                    lineHasRegularCharacters = OptionalBoolean.MAYBE;
                                }
                            }
                            break;
                        case '*':
                            if(prevChar == '/') {
                                // decrement because previous '/' is layout
                                charCountExcludingLayout--;
                                parseState = ParseState.MULTI_LINE_COMMENT;
                            }
                            break;
                        default:
                            if(!isLayoutChar(chr) || (!isLayoutChar(prevChar) && !previousParseState.isComment())) {
                                charCountExcludingLayout++;
                                lineHasRegularCharacters = OptionalBoolean.TRUE;
                            } else if(lineHasRegularCharacters == OptionalBoolean.MAYBE) {
                                lineHasRegularCharacters = OptionalBoolean.TRUE;
                            }
                    }
                    break;
                case STRING:
                    charCountExcludingLayout++;
                    lineHasRegularCharacters = OptionalBoolean.TRUE;
                    if(chr == '"' && prevChar != '\\') {
                        parseState = ParseState.REGULAR;
                    }
                    break;
                case SINGLE_LINE_COMMENT:
                    if(chr == '\n') {
                        if(lineHasRegularCharacters == OptionalBoolean.MAYBE) {
                            lineHasRegularCharacters = OptionalBoolean.FALSE;
                        }
                        parseState = ParseState.REGULAR;
                    }
                    break;
                case MULTI_LINE_COMMENT:
                    if(prevChar == '*' && chr == '/') {
                        if(lineHasRegularCharacters == OptionalBoolean.MAYBE) {
                            lineHasRegularCharacters = OptionalBoolean.FALSE;
                        }
                        parseState = ParseState.REGULAR;
                    }
            }
            if(chr == '\n') {
                lineCount++;
                if(lineHasRegularCharacters == OptionalBoolean.TRUE) {
                    lineCountExcludingLayout++;
                }
                lineHasRegularCharacters = OptionalBoolean.FALSE;
            }
            prevChar = chr;
            previousParseState = curState;
        }
        if(lineHasRegularCharacters == OptionalBoolean.TRUE) {
            lineCountExcludingLayout++;
        }
        return new FileCounts(lineCount, lineCountExcludingLayout, charCount, charCountExcludingLayout);
    }

    private boolean isLayoutChar(int chr) throws IllegalArgumentException {
        if (chr < 0 || chr > 255) {
            throw new IllegalArgumentException("isLayoutChar cannot handle values outside the range 0-255");
        }

        return Character.isWhitespace((char) chr);
    }
}
