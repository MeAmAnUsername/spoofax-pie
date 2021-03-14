package mb.jsglr1.common;

import mb.common.message.KeyedMessages;
import mb.jsglr.common.JSGLRTokens;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

public class JSGLR1ParseOutput implements Serializable {
    public final IStrategoTerm ast;
    public final JSGLRTokens tokens;
    public final KeyedMessages messages;
    public final boolean recovered;

    public JSGLR1ParseOutput(IStrategoTerm ast, JSGLRTokens tokens, KeyedMessages messages, boolean recovered) {
        this.ast = ast;
        this.tokens = tokens;
        this.messages = messages;
        this.recovered = recovered;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JSGLR1ParseOutput that = (JSGLR1ParseOutput)o;
        return recovered == that.recovered &&
            ast.equals(that.ast) &&
            tokens.equals(that.tokens) &&
            messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return Objects.hash(ast, tokens, messages, recovered);
    }

    @Override public String toString() {
        return "JSGLR1ParseOutput{" +
            "ast=" + ast +
            ", tokens=" + tokens +
            ", messages=" + messages +
            ", recovered=" + recovered +
            '}';
    }
}
