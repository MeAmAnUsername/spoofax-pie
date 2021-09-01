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
 * comments) as a single character. Leading and trailing layout is counted.
 */
public class CountFileLinesAndCharacters implements TaskDef<@NonNull ResourcePath, @NonNull Result<FileCounts, @NonNull Exception>> {
    private enum ParseState {
        REGULAR,
        STRING,
        COMMENT_START, // found a '/', check for '/' or '*'
        SINGLE_LINE_COMMENT,
        MULTI_LINE_COMMENT,
        COMMENT_END // found a '*', check for '/'
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
        int charCount = 0;
        int charCountExcludingLayout = 0;

        ParseState parseState = ParseState.REGULAR;
        boolean lineHasRegularCharacters = false;
        boolean prevCharWasLayout = false;
        int prevChar = -1;

        int chr;
        while((chr = stream.read()) != -1) {
            charCount++;
            switch(parseState) {
                case REGULAR:
                    switch(chr) {
                        case '"':
                            charCountExcludingLayout++;
                            lineHasRegularCharacters = true;
                            prevCharWasLayout = false;
                            parseState = ParseState.STRING;
                            break;
                        case '/':
                            charCountExcludingLayout++; // eagerly increment
                            // do not update prevCharWasLayout, used in COMMENT_START
                            parseState = ParseState.COMMENT_START;
                            break;
                        default:
                            boolean isLayout = isLayoutChar(chr);
                            if(!isLayout || !prevCharWasLayout) {
                                charCountExcludingLayout++;
                                lineHasRegularCharacters = true;
                            }
                            prevCharWasLayout = isLayout;
                    }
                    break;
                case STRING:
                    charCountExcludingLayout++;
                    lineHasRegularCharacters = true;
                    prevCharWasLayout = false;
                    if(chr == '"' && prevChar != '\\') {
                        parseState = ParseState.REGULAR;
                    }
                    break;
                case COMMENT_START:
                    boolean chrIsSlash = chr == '/';
                    if(chrIsSlash || chr == '*') {
                        charCountExcludingLayout--; // first '/' was erroneously counted
                        prevCharWasLayout = true;
                        if (chrIsSlash) {
                            parseState = ParseState.SINGLE_LINE_COMMENT;
                        } else {
                            parseState = ParseState.MULTI_LINE_COMMENT;
                        }
                    } else {
                        charCountExcludingLayout++;
                        lineHasRegularCharacters = true;
                        prevCharWasLayout = isLayoutChar(chr);
                        parseState = ParseState.REGULAR;
                    }
                case SINGLE_LINE_COMMENT:
                    if(chr == '\n') {
                        prevCharWasLayout = true;
                        parseState = ParseState.REGULAR;
                    }
                    break;
                case MULTI_LINE_COMMENT:
                    if(chr == '*') {
                        parseState = ParseState.COMMENT_END;
                    }
                    break;
                case COMMENT_END:
                    if (chr == '/') {
                        parseState = ParseState.REGULAR;
                    }
                    break;
                default:
                    throw new IllegalStateException(String.format("Parse state %s is not handled", parseState));
            }
            if(chr == '\n') {
                lineCount++;
                if(lineHasRegularCharacters) {
                    lineCountExcludingLayout++;
                }
                lineHasRegularCharacters = false;
                prevCharWasLayout = true;
            }
            prevChar = chr;
        }
        if(lineHasRegularCharacters) {
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
