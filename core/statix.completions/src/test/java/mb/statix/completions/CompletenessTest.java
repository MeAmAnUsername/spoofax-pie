package mb.statix.completions;

import io.usethesource.capsule.Map;
import mb.jsglr.common.MoreTermUtils;
import mb.log.api.Logger;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.IStringTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.resource.DefaultResourceKey;
import mb.resource.ResourceKey;
import mb.statix.common.SolverContext;
import mb.statix.common.SolverState;
import mb.statix.common.StatixAnalyzer;
import mb.statix.common.StatixSpec;
import mb.statix.solver.Delay;
import mb.statix.solver.IConstraint;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests that the completion algorithm is complete.
 * For a given AST, it must be able to regenerate that AST in a number of completion steps,
 * when presented with the AST with a hole in it.
 */
@SuppressWarnings("SameParameterValue")
public class CompletenessTest {

    private static final SLF4JLoggerFactory loggerFactory = new SLF4JLoggerFactory();
    private static final Logger log = loggerFactory.create(CompletenessTest.class);
    private static final String TESTPATH = "/mb/statix/completions";
    private static final String TIGER_SPEC_PATH = TESTPATH + "/spec.aterm";
    private static final String TIGER_SPEC_SIMPLE1_PATH = TESTPATH + "/simple1/spec.aterm";

    // TODO: Enable
    @TestFactory
    @Disabled
    public List<DynamicTest> completenessTests() {
        //noinspection ArraysAsListWithZeroOrOneArgument
        return Arrays.asList(
//            completenessTest(TESTPATH + "/simple1/test1.aterm", TESTPATH + "/simple1/test1.input.aterm", TIGER_SPEC_SIMPLE1_PATH, "statics", "programOK")
//            completenessTest(TESTPATH + "/test1.aterm", TESTPATH + "/test1.input.aterm", TIGER_SPEC_PATH, "static-semantics", "programOk")
//            completenessTest(TESTPATH + "/test2.aterm", TESTPATH + "/test2.input.aterm", TIGER_SPEC_PATH, "static-semantics", "programOk")
            completenessTest(TESTPATH + "/test3.aterm", TESTPATH + "/test3.input.aterm", TIGER_SPEC_PATH, "static-semantics", "programOk")
//            completenessTest(TESTPATH + "/test4.aterm", TESTPATH + "/test4.input.aterm", TIGER_SPEC_PATH, "static-semantics", "programOk")
        );
    }

    /**
     * Creates a completion test.
     *
     * @param expectedTermPath the resource path to a file with the expected Stratego ATerm
     * @param inputTermPath the resource path to a file with the input Stratego ATerm
     * @param specPath the resource path to a file with the merged Statix spec Stratego ATerm
     * @param specName the name of the specification
     * @param rootRuleName the name of the root rule
     * @return the created test
     */
    private DynamicTest completenessTest(String expectedTermPath, String inputTermPath, String specPath, String specName, String rootRuleName) {
        return DynamicTest.dynamicTest("complete file " + Paths.get(inputTermPath).getFileName() + " to " + Paths.get(expectedTermPath).getFileName() + " using spec " + Paths.get(specPath).getFileName() + "",
            () -> {
                StatixSpec spec = StatixSpec.fromClassLoaderResources(CompletenessTest.class, specPath);
                IStrategoTerm expectedTerm = MoreTermUtils.fromClassLoaderResources(CompletenessTest.class, expectedTermPath);
                IStrategoTerm inputTerm = MoreTermUtils.fromClassLoaderResources(CompletenessTest.class, inputTermPath);
                doCompletenessTest(expectedTerm, inputTerm, spec, specName, rootRuleName);
            });
    }

    /**
     * Performs a completion test.
     *
     * @param expectedTerm the expected Stratego ATerm
     * @param inputTerm the input Stratego ATerm
     * @param spec the merged Statix spec Stratego ATerm
     * @param specName the name of the specification
     * @param rootRuleName the name of the root rule
     */
    private void doCompletenessTest(IStrategoTerm expectedTerm, IStrategoTerm inputTerm, StatixSpec spec, String specName, String rootRuleName) throws InterruptedException, IOException {
        ITermFactory termFactory = new TermFactory();
        StrategoTerms strategoTerms = new StrategoTerms(termFactory);
        ResourceKey resourceKey = new DefaultResourceKey("test", "ast");

        IStrategoTerm annotatedExpectedTerm = StrategoTermIndices.index(expectedTerm, resourceKey.toString(), termFactory);
        ITerm expectedStatixTerm = strategoTerms.fromStratego(annotatedExpectedTerm);

        IStrategoTerm annotatedInputTerm = StrategoTermIndices.index(inputTerm, resourceKey.toString(), termFactory);
        ITerm inputStatixTerm = strategoTerms.fromStratego(annotatedInputTerm);

        doCompletenessTest(expectedStatixTerm, inputStatixTerm, spec, termFactory, resourceKey, specName, rootRuleName);
    }

    /**
     * Performs a completion test.
     *
     * @param expectedTerm the expected NaBL term
     * @param inputTerm the input NaBL term
     * @param spec the merged Statix spec
     * @param termFactory the Stratego term factory
     * @param resourceKey the resource key used to create term indices
     * @param specName the name of the specification
     * @param rootRuleName the name of the root rule
     */
    private void doCompletenessTest(ITerm expectedTerm, ITerm inputTerm, StatixSpec spec, ITermFactory termFactory, ResourceKey resourceKey, String specName, String rootRuleName) throws InterruptedException, IOException {
        TermCompleter completer = new TermCompleter();
        StatixAnalyzer analyzer = new StatixAnalyzer(spec, termFactory, loggerFactory);

        // Preparation
        long prepStartTime = System.nanoTime();
        CompletionExpectation<? extends ITerm> completionExpectation = CompletionExpectation.fromTerm(inputTerm, expectedTerm, resourceKey.toString());

        // Get the solver state of the program (whole project),
        // which should have some remaining constraints on the placeholders.
        SolverContext ctx = analyzer.createContext();
        long analyzeStartTime = System.nanoTime();
        SolverState startState = analyzer.createStartState(completionExpectation.getIncompleteAst(), specName, rootRuleName);
        // TODO: Use withExistentials() to add the placeholder variable(s)
        SolverState initialState = analyzer.analyze(ctx, startState);
        if (initialState.hasErrors()) {
            fail("Completion failed: input program validation failed.\n" + initialState.toString());
            return;
        }
        if (initialState.getConstraints().isEmpty()) {
            fail("Completion failed: no constraints left, nothing to complete.\n" + initialState.toString());
            return;
        }

        long completeStartTime = System.nanoTime();
        int stepCount = 0;
        completionExpectation = completionExpectation.withState(initialState);
        while (!completionExpectation.isComplete()) {

            boolean allDelayed = true;

            // For each term variable, invoke completion
            for(ITermVar var : completionExpectation.getVars()) {
                SolverState state = Objects.requireNonNull(completionExpectation.getState());

                if (isVarInDelays(state.getDelays(), var)) {
                    // We skip variables in delays, let's see where we get until we loop forever.
                    continue;
                } else {
                    allDelayed = false;
                }

                List<TermCompleter.CompletionSolverProposal> proposals = completer.complete(ctx, state, var);
                // For each proposal, find the candidates that fit
                final CompletionExpectation<? extends ITerm> currentCompletionExpectation = completionExpectation;
//                log.info("------------------------------\n" +
//                    "Complete var " + var + " in AST:\n  " + currentCompletionExpectation.getIncompleteAst() + "\n" +
//                    "Expected:\n  " + currentCompletionExpectation.getExpectations().get(var) + "\n" +
//                    "State:\n  " + state);

                final List<CompletionExpectation<? extends ITerm>> candidates = proposals.stream()
                    .map(p -> currentCompletionExpectation.tryReplace(var, p))
                    .filter(Objects::nonNull).collect(Collectors.toList());
                if(candidates.size() == 1) {
                    // Only one candidate, let's apply it
                    completionExpectation = candidates.get(0);
                    log.info("------------------------------\n" +
                        "Complete var " + var + " in AST:\n  " + currentCompletionExpectation.getIncompleteAst() + "\n" +
                        "Expected:\n  " + currentCompletionExpectation.getExpectations().get(var) + "\n" +
                        "State:\n  " + state +
                        "Got 1 candidate:\n  " + candidates.stream().map(c -> c.getState().toString()).collect(Collectors.joining("\n  ")));
                } else if(candidates.size() > 1) {
                    // Multiple candidates, let's use the one with the least number of open variables
                    // and otherwise the first one (could also use the biggest one instead)
                    candidates.sort(Comparator.comparingInt(o -> o.getVars().size()));
                    completionExpectation = candidates.get(0);
                    log.info("------------------------------\n" +
                        "Complete var " + var + " in AST:\n  " + currentCompletionExpectation.getIncompleteAst() + "\n" +
                        "Expected:\n  " + currentCompletionExpectation.getExpectations().get(var) + "\n" +
                        "State:\n  " + state +
                        "Got " + candidates.size() + " candidates:\n  " + candidates.stream().map(c -> c.getState().toString()).collect(Collectors.joining("\n  ")));
                } else if (isLiteral(completionExpectation.getExpectations().get(var))) {
                    // No candidates, but the expected term is a string (probably the name of a declaration).
                    ITerm name = completionExpectation.getExpectations().get(var);
                    @Nullable CompletionExpectation<? extends ITerm> candidate = completionExpectation.tryReplace(var, new TermCompleter.CompletionSolverProposal(completionExpectation.getState(), name));
                    if (candidate == null) {
                        fail(() -> "------------------------------\n" +
                            "Complete var " + var + " in AST:\n  " + currentCompletionExpectation.getIncompleteAst() + "\n" +
                            "Expected:\n  " + currentCompletionExpectation.getExpectations().get(var) + "\n" +
                            "State:\n  " + state +
                            "Got NO candidates, but expected a literal. Could not insert literal " + name + ".\nProposals:\n  " + proposals.stream().map(p -> p.getTerm() + " <-  " + p.getNewState()).collect(Collectors.joining("\n  ")));
                        return;
                    }
                    completionExpectation = candidate;
                    log.info("------------------------------\n" +
                        "Complete var " + var + " in AST:\n  " + currentCompletionExpectation.getIncompleteAst() + "\n" +
                        "Expected:\n  " + currentCompletionExpectation.getExpectations().get(var) + "\n" +
                        "State:\n  " + state +
                        "Got 1 (literal) candidate:\n  " + candidate.getState());
                } else {
                    // No candidates, completion algorithm is not complete
                    fail(() -> "------------------------------\n" +
                        "Complete var " + var + " in AST:\n  " + currentCompletionExpectation.getIncompleteAst() + "\n" +
                        "Expected:\n  " + currentCompletionExpectation.getExpectations().get(var) + "\n" +
                        "State:\n  " + state +
                        "Got NO candidates.\nProposals:\n  " + proposals.stream().map(p -> p.getTerm() + " <-  " + p.getNewState()).collect(Collectors.joining("\n  ")));
                    return;
                }
                stepCount += 1;
            }

            if (allDelayed) {
                // We've been skipping delayed variables but have made no progress. We're stuck.
                @Nullable SolverState state = completionExpectation.getState();
                fail(() -> "Stuck on delaying variables.\nState:\n  " + state);
                return;
            }
        }

        // Done! Success!

        long totalPrepareTime = analyzeStartTime - prepStartTime;
        long totalAnalyzeTime = completeStartTime - analyzeStartTime;
        long totalCompleteTime = System.nanoTime() - completeStartTime;
        long avgDuration = totalCompleteTime / stepCount;
        log.info("Done! Completed {} steps in {} ms, avg. {} ms/step. (Preparation: {} ms, initial analysis: {} ms)", stepCount,
            String.format("%2d", TimeUnit.NANOSECONDS.toMillis(totalCompleteTime)),
            String.format("%2d", TimeUnit.NANOSECONDS.toMillis(avgDuration)),
            String.format("%2d", TimeUnit.NANOSECONDS.toMillis(totalPrepareTime)),
            String.format("%2d", TimeUnit.NANOSECONDS.toMillis(totalAnalyzeTime))
        );
    }

    private static boolean isVarInDelays(Map.Immutable<IConstraint, Delay> delays, ITermVar var) {
        return delays.keySet().stream().anyMatch(c -> c.getVars().contains(var));
    }

    private static boolean isLiteral(ITerm term) {
        // Is it a literal term, or an injection of a literal term?
        return term instanceof IStringTerm
            // TODO: Not use heuristics here.
            || (term instanceof IApplTerm && ((IApplTerm)term).getOp().contains("-LEX2"));
    }

}
