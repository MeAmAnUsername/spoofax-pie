package mb.statix.completions;

import io.usethesource.capsule.Map;
import mb.jsglr.common.MoreTermUtils;
import mb.log.api.Level;
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
import mb.sequences.Seq;
import mb.statix.common.PlaceholderVarMap;
import mb.statix.common.SolverContext;
import mb.statix.common.SolverState;
import mb.statix.common.StatixAnalyzer;
import mb.statix.common.StatixSpec;
import mb.statix.common.strategies.InferStrategy;
import mb.statix.constraints.CAstId;
import mb.statix.constraints.CAstProperty;
import mb.statix.constraints.CEqual;
import mb.statix.constraints.messages.IMessage;
import mb.statix.constraints.messages.MessageKind;
import mb.statix.solver.Delay;
import mb.statix.solver.IConstraint;
import mb.statix.spec.Spec;
import mb.strategies.DebugStrategy;
import mb.strategies.StrategyEventHandler;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests that the completion algorithm is complete.
 * For a given AST, it must be able to regenerate that AST in a number of completion steps,
 * when presented with the AST with a hole in it.
 */
@SuppressWarnings("SameParameterValue")
public abstract class CompletenessTest {

    private static final SLF4JLoggerFactory loggerFactory = new SLF4JLoggerFactory();
    private static final Logger log = loggerFactory.create(CompletenessTest.class);
    protected static final String TESTPATH = "/mb/statix/completions";


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
    protected DynamicTest completenessTest(String expectedTermPath, String inputTermPath, String specPath, String specName, String csvPath, String rootRuleName) {
        return DynamicTest.dynamicTest("complete file " + Paths.get(inputTermPath).getFileName() + " to " + Paths.get(expectedTermPath).getFileName() + " using spec " + Paths.get(specPath).getFileName() + "",
            () -> {
                StatixSpec spec = StatixSpec.fromClassLoaderResources(CompletenessTest.class, specPath);
                IStrategoTerm expectedTerm = MoreTermUtils.fromClassLoaderResources(CompletenessTest.class, expectedTermPath);
                IStrategoTerm inputTerm = MoreTermUtils.fromClassLoaderResources(CompletenessTest.class, inputTermPath);
                doCompletenessTest(expectedTerm, inputTerm, spec, specName, rootRuleName, expectedTermPath, inputTermPath, csvPath);
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
    private void doCompletenessTest(IStrategoTerm expectedTerm, IStrategoTerm inputTerm, StatixSpec spec, String specName, String rootRuleName, String expectedTermPath, String inputTermPath, String csvPath) throws InterruptedException, IOException {
        ITermFactory termFactory = new TermFactory();
        StrategoTerms strategoTerms = new StrategoTerms(termFactory);
        ResourceKey resourceKey = new DefaultResourceKey("test", "ast");

        IStrategoTerm annotatedExpectedTerm = StrategoTermIndices.index(expectedTerm, resourceKey.toString(), termFactory);
        ITerm expectedStatixTerm = strategoTerms.fromStratego(annotatedExpectedTerm);

        IStrategoTerm annotatedInputTerm = StrategoTermIndices.index(inputTerm, resourceKey.toString(), termFactory);
        ITerm inputStatixTerm = strategoTerms.fromStratego(annotatedInputTerm);

        doCompletenessTest(expectedStatixTerm, inputStatixTerm, spec, termFactory, resourceKey, specName, rootRuleName, expectedTermPath, csvPath);
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
    private void doCompletenessTest(ITerm expectedTerm, ITerm inputTerm, StatixSpec spec, ITermFactory termFactory, ResourceKey resourceKey, String specName, String rootRuleName, String testName, String csvPath) throws InterruptedException, IOException {
        StatsGatherer stats = new StatsGatherer(csvPath);
        TermCompleter completer = new TermCompleter();
        StatixAnalyzer analyzer = new StatixAnalyzer(spec, termFactory, loggerFactory);
        precomputeOrderIndependentRules(spec.getSpec());
        ExecutorService executorService = Executors.newCachedThreadPool();

        // Preparation
        stats.startTest(testName);
        PlaceholderVarMap placeholderVarMap = new PlaceholderVarMap(resourceKey.toString());
        CompletionExpectation<? extends ITerm> completionExpectation = CompletionExpectation.fromTerm(inputTerm, expectedTerm, placeholderVarMap);

        try(final StrategyEventHandler eventHandler = StrategyEventHandler.none()) {// new DebugEventHandler(Paths.get("debug.yml"))) {
            // Get the solver state of the program (whole project),
            // which should have some remaining constraints on the placeholders.
            SolverContext ctx = analyzer.createContext(eventHandler).withReporters(
                t -> stats.reportSubTime(0, t),
                t -> stats.reportSubTime(1, t),
                t -> stats.reportSubTime(2, t),
                t -> stats.reportSubTime(3, t));
            stats.startInitialAnalysis();
            SolverState startState = analyzer.createStartState(completionExpectation.getIncompleteAst(), specName, rootRuleName)
                .withExistentials(placeholderVarMap.getVars())
                .precomputeCriticalEdges(ctx.getSpec());
            SolverState initialState = analyzer.analyze(ctx, startState);

            // We track the current collection of errors.
            final List<java.util.Map.Entry<IConstraint, IMessage>> currentErrors = initialState.getMessages().entrySet().stream().filter(kv -> kv.getValue().kind() == MessageKind.ERROR).collect(Collectors.toList());
            if(!currentErrors.isEmpty()) {
                //log.warn("input program validation failed.\n"+ initialState);
                fail("Completion failed: input program validation failed.\n" + initialState.toString());
                return;
            }

            if(initialState.getConstraints().isEmpty()) {
                fail("Completion failed: no constraints left, nothing to complete.\n" + initialState);
                return;
            }

            final SolverContext newCtx = ctx.withAllowedErrors(currentErrors);

            // We use a heuristic here.
            final Predicate<ITerm> isInjPredicate = t -> t instanceof IApplTerm && ((IApplTerm)t).getArity() == 1 && ((IApplTerm)t).getOp().contains("2");

            completionExpectation = completionExpectation.withState(initialState);

            // Perform a left-to-right depth-first search of completions:
            // - For each incomplete variable, we perform completion.
            // - If any of the variables result in one candidate, this candidate is applied.
            // - If none of the variables results in one candidate (i.e., there's no progress),
            //     then we try inserting a literal at the first available spot.
            // - If we cannot make progress and cannot insert literals,
            //     then completion fails.

            // List of failed variables
            final Set<ITermVar> failedVars = new HashSet<>();
            // List of delayed variables
            final Set<ITermVar> delayedVars = new HashSet<>();
            // Whether we did anything useful since the last time we tried all delays
            boolean progressedSinceDelays = false;
            boolean insertedSinceFailure = false;
            while(!completionExpectation.isComplete()) {
                cleanup();

                // Pick the next variable that is not delayed or failed
                ITermVar var = completionExpectation.getVars().stream().filter(v -> !failedVars.contains(v) && !delayedVars.contains(v)).findFirst().orElse(null);
                if (var != null) {
                    CompletionRunnable runnable = new CompletionRunnable(completer, completionExpectation, var, stats, newCtx, isInjPredicate, testName);

                    Future<CompletionResult> future = executorService.submit(runnable);
                    try {
//                        CompletionResult result = future.get();
                        CompletionResult result = future.get(5, TimeUnit.SECONDS);
                        switch(result.state) {
                            case Inserted:
                                insertedSinceFailure = true;
                                // Fallthrough:
                            case Success:
                                progressedSinceDelays = true;
                                completionExpectation = result.getCompletionExpectation();
                                break;
                            case Skip:
                                log.info("Delayed {}", var);
                                delayedVars.add(var);
                                break;
                            case Fail:
                                log.info("Failed {}", var);
                                failedVars.add(var);
                                break;
                        }
                    } catch(TimeoutException ex) {
                        fail(() -> "Interrupted.");
                        return;
                    } catch(ExecutionException ex) {
                        log.error("Error was thrown: " + ex.getMessage(), ex);
                        fail(() -> "Error was thrown.");
                        return;
                    }
                } else if (progressedSinceDelays && !delayedVars.isEmpty()) {
                    // Try all delayed variables again
                    log.warn("All variables delayed, trying again.");
                    delayedVars.clear();
                    progressedSinceDelays = false;
                    continue;
                } else if (insertedSinceFailure && !failedVars.isEmpty()) {
                    log.warn("All variables delayed or rejected, retrying since new literals have been inserted.");
                    // Try again on all completion variables
                    failedVars.clear();
                    insertedSinceFailure = false;
                    continue;
                } else {
                    // No literals to insert
                    fail("All completions failed and could not insert any literals. The following are waiting: " + String.join(", ", failedVars.stream().map(Object::toString).collect(Collectors.toList())));
                    break;
                }
            }
            log.info("Done completing!");
        }

        // Done! Success!
        stats.endTest();
    }

    private void precomputeOrderIndependentRules(Spec spec) {
        log.info("Precomputing...");
        long start = System.nanoTime();
        for(String ruleName : spec.rules().getRuleNames()) {
            spec.rules().getOrderIndependentRules(ruleName);
        }
        log.info("Precomputed order independent rules in " + ((System.nanoTime() - start) / 1000000) + " ms");

    }

    private static void logCompletionStepResult(Level level, String message, String testName, ITermVar var, CompletionExpectation<?> expectation) {
        log.log(level, "-------------- " + testName +" ----------------\n" +
            endWithNewline(message) +
            "Completion of var " + var + " in AST:\n  " + expectation.getIncompleteAst() +
            "\nExpected:\n  " + expectation.getExpectations().get(var));
//            "\nState:\n  " + expectation.getState());
    }

    private static void logCompletionStepResultWithProposals(Level level, String message, String testName, ITermVar var, CompletionExpectation<?> expectation, List<TermCompleter.CompletionSolverProposal> proposals) {
        logCompletionStepResult(level, message + "\nProposals:\n  " + proposals.stream().map(p -> p.getTerm() + " <-  " + p.getNewState()).collect(Collectors.joining("\n  ")), testName, var, expectation);
    }

    private static void logCompletionStepResultWithCandidates(Level level, String message, String testName, ITermVar var, CompletionExpectation<?> expectation, List<CompletionExpectation<? extends ITerm>> candidates) {
        logCompletionStepResult(level, message + "\nGot " + candidates.size() + " candidate" + (candidates.size() == 1 ? "" : "s") + ":\n  " + (DebugStrategy.debug ? candidates.stream().map(c -> c.getState().toString()).collect(Collectors.joining("\n  ")) : ""), testName, var, expectation);
    }
    // List<TermCompleter.CompletionSolverProposal> proposals

    private static String endWithNewline(String s) {
        if (s == null || s.isEmpty() || s.trim().isEmpty()) return "";
        if (s.endsWith("\n")) return s;
        return s + "\n";
    }

    private static void cleanup() {
        log.info("Cleaning...");
        long cleanStart = System.nanoTime();
        System.gc();
        System.runFinalization();
        System.gc();
        log.info("Cleaned in " + ((System.nanoTime() - cleanStart) / 1000000) + " ms");
        Runtime runtime = Runtime.getRuntime();
        NumberFormat format = NumberFormat.getInstance();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        log.info("Free memory: {} MB", freeMemory / (1024 * 1024));
        log.info("Allocated memory: {} MB", allocatedMemory / (1024 * 1024));
        log.info("Max memory: {} MB", maxMemory / (1024 * 1024));
        log.info("Total free memory: {} MB", (freeMemory + (maxMemory - allocatedMemory)) / (1024 * 1024));
    }

    private static boolean isVarInDelays(Map.Immutable<IConstraint, Delay> delays, ITermVar var) {
        return delays.values().stream().anyMatch(d -> d.vars().contains(var));
//        return delays.keySet().stream().anyMatch(c -> c.getVars().contains(var));
    }

    private static boolean isLiteral(ITerm term) {
        // Is it a literal term, or an injection of a literal term?
        return term instanceof IStringTerm
            // TODO: Not use heuristics here.
            || (term instanceof IApplTerm && ((IApplTerm)term).getOp().contains("-LEX2"));
    }


    private static class CompletionRunnable implements Callable<CompletionResult> {

        private static final Logger log = loggerFactory.create(CompletionRunnable.class);
        private final TermCompleter completer;
        private final CompletionExpectation<? extends ITerm> completionExpectation;
        private final ITermVar var;
        private final StatsGatherer stats;
        private final SolverContext newCtx;
        private final Predicate<ITerm> isInjPredicate;
        private final String testName;


        private CompletionRunnable(
            TermCompleter completer,
            CompletionExpectation<? extends ITerm> completionExpectation,
            ITermVar var,
            StatsGatherer stats,
            SolverContext newCtx,
            Predicate<ITerm> isInjPredicate,
            String testName
        ) {
            this.completer = completer;
            this.completionExpectation = completionExpectation;
            this.var = var;
            this.stats = stats;
            this.newCtx = newCtx;
            this.isInjPredicate = isInjPredicate;
            this.testName = testName;
        }


        @Override
        public CompletionResult call() throws InterruptedException {
            try {
                stats.startRound();
                final CompletionExpectation<? extends ITerm> newCompletionExpectation;
                final SolverState state = Objects.requireNonNull(completionExpectation.getState());

                log.info("====================== " + testName +" ================================\n" +
                    "COMPLETING var " + var + " in AST:\n  " + completionExpectation.getIncompleteAst() + "\n" +
                    "Expected:\n  " + completionExpectation.getExpectations().get(var) + "\n" +
                    "State:\n  " + state);

                if (state.getConstraints().stream().filter(c -> c.getVars().contains(var))
                        .anyMatch(CompletenessTest::isLiteralAstProperty) &&
                        isLiteral(completionExpectation.getExpectations().get(var))) {
                    // The variable is a literal that has an @decl or @lit annotation.
                    log.info("Found declaration name or literal, inserting...");
                    ITerm term = completionExpectation.getExpectations().get(var);

                    // Add a constraint that inserts the literal, and perform inference
                    CEqual ceq = new CEqual(var, term);
                    SolverState newSolverState = completionExpectation.getState().updateConstraints(newCtx.getSpec(), Collections.singletonList(ceq), Collections.emptyList());
                    List<SolverState> inferredSolverStates = InferStrategy.getInstance().eval(newCtx, newSolverState).toList().eval();
                    if (inferredSolverStates.isEmpty()) {
                        logCompletionStepResult(Level.Warn, "Inference failed when inserting literal. Could not insert: " + term + "\n",
                            testName, var, completionExpectation);
                        stats.endRound();
                        return CompletionResult.fail();
                    }
                    SolverState inferredSolverState = inferredSolverStates.get(0);

                    // Replace the term in the expectation
                    @Nullable CompletionExpectation<? extends ITerm> candidate = completionExpectation.tryReplace(var, new TermCompleter.CompletionSolverProposal(inferredSolverState, term));
                    if(candidate == null) {
                        logCompletionStepResult(Level.Warn, "Expected a declaration name or literal. Could not insert: " + term + "\n",
                            testName, var, completionExpectation);
                        stats.endRound();
                        return CompletionResult.fail();
                    }
                    stats.insertedLiteral();
                    newCompletionExpectation = candidate;
                    logCompletionStepResult(Level.Info, "Inserted 1 declaration name or literal: " + term + "\n " + candidate.getIncompleteAst(),
                        testName, var, completionExpectation);
                    stats.endRound();
                    return CompletionResult.inserted(newCompletionExpectation);
                }

                if(isVarInDelays(state.getDelays(), var)) {
                    // We skip variables in delays, let's see where we get until we loop forever.
                    stats.skipRound();
                    log.info("All delayed. Skipped.");
                    return CompletionResult.skip();
                }

                List<TermCompleter.CompletionSolverProposal> proposals = completer.complete(newCtx, isInjPredicate, state, var);
                // For each proposal, find the candidates that fit
                final CompletionExpectation<? extends ITerm> currentCompletionExpectation = completionExpectation;

                final List<CompletionExpectation<? extends ITerm>> candidates = proposals.stream()
                    .map(p -> currentCompletionExpectation.tryReplace(var, p))
                    .filter(Objects::nonNull).collect(Collectors.toList());
                if(candidates.size() == 1) {
                    // Only one candidate, let's apply it
                    newCompletionExpectation = candidates.get(0);
                    logCompletionStepResultWithCandidates(Level.Info, "Only one candidate.", testName, var, currentCompletionExpectation, candidates);
                } else if(candidates.size() > 1) {
                    // Multiple candidates, let's use the one with the least number of open variables
                    // and otherwise the first one (could also use the biggest one instead)
                    candidates.sort(Comparator.comparingInt(o -> o.getVars().size()));
                    newCompletionExpectation = candidates.get(0);
                    logCompletionStepResultWithCandidates(Level.Info, "Multiple candidates, picked the first.", testName, var, currentCompletionExpectation, candidates);
                } else {
                    // No candidates, completion algorithm is not complete
                    logCompletionStepResultWithProposals(Level.Warn, "Got NO candidates.", testName, var, currentCompletionExpectation, proposals);
                    stats.endRound();
                    return CompletionResult.fail();
                }
                stats.endRound();
                return CompletionResult.of(newCompletionExpectation);
            } catch(Throwable ex) {
                log.error("Uncaught exception: " + ex.getMessage(), ex);
                throw ex;
            }
        }
    }

    private static boolean isLiteralAstProperty(IConstraint c) {
        if (!(c instanceof CAstProperty)) return false;
        CAstProperty astProperty = (CAstProperty)c;
        ITerm propertyTerm = astProperty.property();
        if (!(propertyTerm instanceof IApplTerm)) return false;
        IApplTerm propertyAppl = (IApplTerm)propertyTerm;
        if (!propertyAppl.getOp().equals("Prop") || propertyAppl.getArity() != 1) return false;
        ITerm propertyArg = propertyAppl.getArgs().get(0);
        if (!(propertyArg instanceof IStringTerm)) return false;
        String propertyName = ((IStringTerm)propertyArg).getValue();
        // Declarations are marked `@name.decl := name`.
        // Literals (int, string) are marked `@name.lit := name`.
        return propertyName.equals("decl") || propertyName.equals("lit");
    }

    private enum CompletionState {
        Success,
        Fail,
        Skip,
        Inserted,       // When a literal has been inserted
    }

    private static class CompletionResult {
        private final CompletionState state;
        private final CompletionExpectation<? extends ITerm> completionExpectation;

        public CompletionResult(CompletionState state, CompletionExpectation<? extends ITerm> completionExpectation) {
            this.state = state;
            this.completionExpectation = completionExpectation;
        }

        public CompletionExpectation<? extends ITerm> getCompletionExpectation() {
            return completionExpectation;
        }

        public CompletionState getState() {
            return state;
        }

        public static CompletionResult fail() { return new CompletionResult(CompletionState.Fail, null); }
        public static CompletionResult skip() { return new CompletionResult(CompletionState.Skip, null); }
        public static CompletionResult inserted(CompletionExpectation<? extends ITerm> completionExpectation) { return new CompletionResult(CompletionState.Inserted, completionExpectation); }
        public static CompletionResult of(CompletionExpectation<? extends ITerm> completionExpectation) { return new CompletionResult(CompletionState.Success, completionExpectation); }

    }

}
