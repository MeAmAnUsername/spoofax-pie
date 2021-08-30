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

public class CountFileCharacters implements TaskDef<@NonNull ResourcePath, @NonNull Result<FileCounts, @NonNull Exception>> {
    private enum ParseState {
        REGULAR,
        STRING,
        SINGLE_LINE_COMMENT,
        MULTI_LINE_COMMENT
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
                int charCount = 0;
                int charCountExcludingLayout = 0;
                ParseState parseState = ParseState.REGULAR;
                int prevChar = 'a';
                int chr;
                while((chr = inputStream.read()) != -1) {
                    charCount++;
                    switch (parseState) {
                        case REGULAR:
                            if (!isLayoutChar(chr) || !isLayoutChar(prevChar)) {
                                charCountExcludingLayout++;
                            }
                            switch (chr) {
                                case '"':
                                    parseState = ParseState.STRING;
                                    break;
                                case '/':
                                    if (prevChar == '/') {
                                        parseState = ParseState.SINGLE_LINE_COMMENT;
                                    }
                                    break;
                                case '*':
                                    if (prevChar == '/') {
                                        parseState = ParseState.MULTI_LINE_COMMENT;
                                    }
                                    break;
                            }
                            break;
                        case STRING:
                            charCountExcludingLayout++;
                            if (chr == '"' && prevChar != '\\') {
                                parseState = ParseState.REGULAR;
                            }
                            break;
                        case SINGLE_LINE_COMMENT:
                            if (chr == '\n') {
                                parseState = ParseState.REGULAR;
                            }
                            break;
                        case MULTI_LINE_COMMENT:
                            if (prevChar == '*' && chr == '/') {
                                parseState = ParseState.REGULAR;
                            }
                    }
                    prevChar = chr;
                }
                return Result.ofOk(new FileCounts(charCount, charCountExcludingLayout));
            }
        } catch(IOException | IllegalArgumentException e) {
            return Result.ofErr(e);
        }
    }

    private boolean isLayoutChar(int chr) throws IllegalArgumentException {
        if (chr < 0 || chr > 255) {
            throw new IllegalArgumentException("isLayoutChar cannot handle values outside the range 0-255");
        }

        return Character.isWhitespace((char) chr);
    }
}
