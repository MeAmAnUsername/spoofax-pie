package mb.statix.completions;

import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.Terms;
import mb.nabl2.terms.build.TermBuild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Shoots one or more holes in an AST.
 */
public final class AstShooter {

    private final Random rng;
    private final String resourceName;
    private final Supplier<String> freshNameSupplier;

    public AstShooter(Random rng, String resourceName, Supplier<String> freshNameSupplier) {
        this.rng = rng;
        this.resourceName = resourceName;
        this.freshNameSupplier = freshNameSupplier;
    }


    public CompletionExpectation<? extends ITerm> shootHoleInTerm(final ITerm term, final int level) {
        // Finds a random term in the AST and replaces it by a placeholder.
        if (rng.nextInt(level) == 0) {
            // We replace this term by a placeholder
            ITermVar newVar = TermBuild.B.newVar(resourceName, freshNameSupplier.get());
            HashMap<ITermVar, ITerm> expectedAsts = new HashMap<>();
            expectedAsts.put(newVar, term);
            return CompletionExpectation.of(newVar, expectedAsts, null);
        } else {
            // We continue one more level
            // Let's find where we need to go
            return term.match(Terms.cases(
                appl -> {
                    // We copy all the args and replace one of the args at random
                    if (appl.getArity() > 0) {
                        int index = rng.nextInt(appl.getArity());
                        ArrayList<ITerm> args = new ArrayList<>(appl.getArgs());
                        CompletionExpectation<? extends ITerm> newTerm = shootHoleInTerm(args.get(index), level - 1);
                        args.set(index, newTerm.getIncompleteAst());
                        return CompletionExpectation.of(TermBuild.B.newAppl(appl.getOp(), args, appl.getAttachments()), newTerm.getExpectations(), null);
                    } else {
                        return CompletionExpectation.of(appl, Collections.emptyMap(), null);
                    }
                },
                list -> {
                    // We replace one of the elements at random
                    int index = rng.nextInt(list.getMinSize());
                    return shootHoleInListTerm(list, index, level - 1);
                },
                string -> CompletionExpectation.of(string, Collections.emptyMap(), null),
                integer -> CompletionExpectation.of(integer, Collections.emptyMap(), null),
                blob -> CompletionExpectation.of(blob, Collections.emptyMap(), null),
                var -> CompletionExpectation.of(var, Collections.emptyMap(), null)
            ));
        }
    }

    public CompletionExpectation<? extends IListTerm> shootHoleInListTerm(final IListTerm list, final int index, final int level) {
        return list.match(ListTerms.cases(
            cons -> {
                if(index == 0) {
                    CompletionExpectation<? extends ITerm> head = shootHoleInTerm(cons.getHead(), level - 1);
                    return CompletionExpectation.of(TermBuild.B.newCons(head.getIncompleteAst(), cons.getTail(), cons.getAttachments()), head.getExpectations(), null);
                } else {
                    CompletionExpectation<? extends IListTerm> tail = shootHoleInListTerm(cons.getTail(), index - 1, level - 1);
                    return CompletionExpectation.of(TermBuild.B.newCons(cons.getHead(), tail.getIncompleteAst(), cons.getAttachments()), tail.getExpectations(), null);
                }
            },
            nil -> CompletionExpectation.of(nil, Collections.emptyMap(), null),
            var -> CompletionExpectation.of(var, Collections.emptyMap(), null)
        ));
    }


}
