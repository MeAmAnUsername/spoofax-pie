package mb.tiger.spoofax.task.reusable;

import mb.common.style.StyleName;
import mb.common.util.ListView;
import mb.completions.common.CompletionProposal;
import mb.completions.common.CompletionResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.tiger.spoofax.TigerScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.util.Objects;

@TigerScope
public class TigerCompleteTaskDef implements TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable CompletionResult> {
    @Inject
    public TigerCompleteTaskDef() {}

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public @Nullable CompletionResult exec(ExecContext context, Supplier<@Nullable IStrategoTerm> astProvider) throws Exception {
        @Nullable IStrategoTerm ast = astProvider.get(context);
        if (ast == null) return null;

        return new CompletionResult(ListView.of(
            new CompletionProposal("mypackage", "description", "", "", "mypackage", Objects.requireNonNull(StyleName.fromString("meta.package")), ListView.of(), false),
            new CompletionProposal("myclass", "description", "", "T", "mypackage", Objects.requireNonNull(StyleName.fromString("meta.class")), ListView.of(), false)
        ), true);
    }
}
