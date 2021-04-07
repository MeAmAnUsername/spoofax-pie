package mb.strategies;

import mb.sequences.Seq;

import java.io.IOException;

/**
 * Provides initial values, ignoring the input.
 *
 * @param <CTX> the type of context
 * @param <I> the type of input
 * @param <O> the type of outputs
 */
public final class BuildStrategy<CTX, I, O> extends AbstractStrategy1<CTX, Iterable<O>, I, O>{

    @SuppressWarnings("rawtypes")
    private static final BuildStrategy instance = new BuildStrategy();
    @SuppressWarnings("unchecked")
    public static <CTX, I, O> BuildStrategy<CTX, I, O> getInstance() { return (BuildStrategy<CTX, I, O>)instance; }

    private BuildStrategy() {}

    @Override
    public String getName() { return "build"; }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public String getParamName(int index) {
        switch (index) {
            case 0: return "iterable";
            default: return super.getParamName(index);
        }
    }

    @Override
    protected Seq<O> innerEval(
        CTX ctx,
        Iterable<O> iterable,
        I input
    ) {
        return Seq.from(iterable);
    }

}
