package mb.statix.completions;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.matching.TermPattern;
import mb.statix.common.PlaceholderVarMap;
import mb.statix.common.StrategoPlaceholders;
import mb.nabl2.terms.substitution.ISubstitution;
import mb.nabl2.terms.substitution.PersistentSubstitution;
import mb.statix.common.SolverState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An incomplete AST,
 * and a mapping from term variables to their expected ASTs.
 */
@Value.Immutable(builder = false)
abstract class AbstractCompletionExpectation<T extends ITerm> {

    public static CompletionExpectation<? extends ITerm> fromTerm(ITerm incompleteTerm, ITerm completeTerm, PlaceholderVarMap placeholderVarMap) {
        // Gather all the placeholders in the term
        ITerm replacedTerm = StrategoPlaceholders.replacePlaceholdersByVariables(incompleteTerm, placeholderVarMap);
        // Does the term we got, including variables, match the expected term?
        Optional<ISubstitution.Immutable> optSubstitution = TermPattern.P.fromTerm(replacedTerm).match(completeTerm);
        if (!optSubstitution.isPresent()) throw new IllegalStateException("The incomplete term is not a match to the complete term.");
        // Yes, and the substitution shows the new variables and their expected term values
        ISubstitution.Immutable substitution = optSubstitution.get();
        HashMap<ITermVar, ITerm> expectedAsts = new HashMap<>();
        for(Map.Entry<ITermVar, ITerm> entry : substitution.entrySet()) {
            expectedAsts.put(entry.getKey(), entry.getValue());
        }
        return CompletionExpectation.of(replacedTerm, expectedAsts, null);
    }

    /**
     * Gets the AST that is being built.
     *
     * @return the incomplete AST
     */
    @Value.Parameter public abstract T getIncompleteAst();

    /**
     * Gets the expected values for the various placeholders in the term.
     *
     * @return the expectations
     */
    @Value.Parameter public abstract Map<ITermVar, ITerm> getExpectations();

    /**
     * Gets the solver state of the incomplete AST.
     *
     * @return the solver state
     */
    @Value.Parameter public abstract @Nullable SolverState getState();

    /**
     * Gets the set of term variables for which we need to find completions.
     *
     * @return the set of term variables; or an empty set when completion is done
     */
    @Value.Derived public Set<ITermVar> getVars() {
        return getExpectations().keySet();
    }

    /**
     * Whether the AST is complete.
     *
     * @return {@code true} when the AST is complete; otherwise, {@code false}
     */
    @Value.Derived public boolean isComplete() {
        return getVars().isEmpty();
    }

    /**
     * Replaces the specified term variable with the specified term,
     * if it is the term that we expected.
     *
     * @param var the term variable to replace
     * @param proposal the proposal to replace it with, which may contain term variables
     * @return the resulting incomplete AST if replacement succeeded; otherwise, {@code null} when it doesn't fit
     */
    public @Nullable CompletionExpectation<? extends ITerm> tryReplace(ITermVar var, TermCompleter.CompletionSolverProposal proposal) {
        ITerm term = proposal.getTerm();
        if (var.equals(term)) {
            // Trying to replace by the same variable indicates that the proposal
            // did not replace the variable by a term.
//            if (getState() != null && getState().getConstraints().intersect(proposal.getNewState().getConstraints()).isEmpty()) {
//                // This is allowed iff the new proposal's constraints
//                // are different from the current state's constraints.
            return null;
//            }
        }

        ISubstitution.@Nullable Immutable substitution = trySubtitute(var, term);
        if (substitution == null) {
            // The variable can never be replaced by the actual term,
            // so we reject this proposal.
            return null;
        }

        // Additionally, we can only accept a proposal if the other variables can be matched to their new values,
        // or where the new value is the same as the old value, the new value is a variable, or the new value is unknown.
        for (ITermVar v : this.getVars()) {
            if (v.equals(var)) continue;
            ITerm actualTerm = proposal.getNewState().project(v);
            boolean matches = trySubtitute(v, actualTerm) != null;
            if (!matches) {
                // The variable can never be replaced by the value in the unifier,
                // so we reject this proposal.
                return null;
            }
        }

        // The substitution shows the new variables and their expected term values
        HashMap<ITermVar, ITerm> expectedAsts = new HashMap<>(this.getExpectations());
        expectedAsts.remove(var);
        for(Map.Entry<ITermVar, ITerm> entry : substitution.entrySet()) {
            expectedAsts.put(entry.getKey(), entry.getValue());
        }
        ITerm newIncompleteAst = PersistentSubstitution.Immutable.of(var, term).apply(getIncompleteAst());
        return CompletionExpectation.of(newIncompleteAst, expectedAsts, proposal.getNewState());
    }

    public ISubstitution.@Nullable Immutable trySubtitute(ITermVar var, ITerm actualTerm) {
        ITerm expectedTerm = getExpectations().get(var);
        assert expectedTerm != null;
        // Does the term we got, including variables, match the expected term?
        return TermPattern.P.fromTerm(actualTerm).match(expectedTerm).orElse(null);
    }
}
