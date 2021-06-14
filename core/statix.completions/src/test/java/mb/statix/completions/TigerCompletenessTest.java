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
import mb.statix.common.PlaceholderVarMap;
import mb.statix.common.SolverContext;
import mb.statix.common.SolverState;
import mb.statix.common.StatixAnalyzer;
import mb.statix.common.StatixSpec;
import mb.statix.constraints.messages.IMessage;
import mb.statix.constraints.messages.MessageKind;
import mb.statix.solver.Delay;
import mb.statix.solver.IConstraint;
import mb.strategies.StrategyEventHandler;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests that the completion algorithm is complete.
 * For a given AST, it must be able to regenerate that AST in a number of completion steps,
 * when presented with the AST with a hole in it.
 */
@SuppressWarnings("SameParameterValue")
public class TigerCompletenessTest extends CompletenessTest {

    private static final String TIGER_CSV_OUTPUT_PATH = "/Users/daniel/repos/spoofax3/devenv-cc/spoofax.pie/core/statix.completions/src/test/resources/mb/statix/completions/tiger";
    private static final String ATERM_EXT = ".aterm";
    private static final String CSV_EXT = ".csv";

    @TestFactory
    @Disabled
    public List<DynamicTest> completenessTests() {
        return Stream.of(
            "test1.tig",
            "test2.tig",
            "test3.tig",
            "test4.tig",
            "test5.tig",

            "appel/test01.tig",
            "appel/test02.tig",
            "appel/test03.tig",
            "appel/test04.tig",
            "appel/test05.tig",
            "appel/test06.tig",
            "appel/test07.tig",
            "appel/test08.tig",
//            "appel/test09.tig",                     // excluded because of semantic error
//            "appel/test10.tig",                     // excluded because of semantic error
//            "appel/test11.tig",                     // excluded because of semantic error
            "appel/test12.tig",
//            "appel/test13.tig",                     // excluded because of semantic error
//            "appel/test14.tig",                     // excluded because of semantic error
//            "appel/test15.tig",                     // excluded because of semantic error
            "appel/test16.tig",
//            "appel/test17.tig",                     // excluded because of semantic error
//            "appel/test18.tig",                     // excluded because of semantic error
//            "appel/test19.tig",                     // excluded because of semantic error
//            "appel/test20.tig",                     // excluded because of semantic error
//            "appel/test21.tig",                     // excluded because of semantic error
//            "appel/test22.tig",                     // excluded because of semantic error
//            "appel/test23.tig",                     // excluded because of semantic error
//            "appel/test24.tig",                     // excluded because of semantic error
//            "appel/test25.tig",                     // excluded because of semantic error
//            "appel/test26.tig",                     // excluded because of semantic error
            "appel/test27.tig",
//            "appel/test28.tig",                     // excluded because of semantic error
//            "appel/test29.tig",                     // excluded because of semantic error
            "appel/test30.tig",
//            "appel/test31.tig",                     // excluded because of semantic error
//            "appel/test32.tig",                     // excluded because of semantic error
//            "appel/test33.tig",                     // excluded because of semantic error
//            "appel/test34.tig",                     // excluded because of semantic error
//            "appel/test35.tig",                     // excluded because of semantic error
//            "appel/test36.tig",                     // excluded because of semantic error
            "appel/test37.tig",
//            "appel/test38.tig",                     // excluded because of semantic error
//            "appel/test39.tig",                     // excluded because of semantic error
//            "appel/test40.tig",                     // excluded because of semantic error
            "appel/test41.tig",
            "appel/test42.tig",
//            "appel/test43.tig",                     // excluded because of semantic error
            "appel/test44.tig",
//            "appel/test45.tig",                     // excluded because of semantic error
            "appel/test47.tig",
            "appel/test48.tig",

            "examples/arith.tig",
//            "examples/fac-error.tig",               // excluded because of semantic error
            "examples/fact-anf.tig",
            "examples/fact-anf2.tig",
            "examples/fact-resolvetest.tig",
            "examples/for.tig",
//            "examples/let-binding.tig",             // excluded because of semantic error
//            "examples/list-type.tig",               // excluded because of semantic error
            "examples/nested.tig",
            "examples/point.tig",
            "examples/queens.tig",
            "examples/rec-types.tig",
//            "examples/record-errors.tig",           // excluded because of semantic error
            "examples/recursion.tig",
//            "examples/redeclarations.tig",          // excluded because of semantic error
            "examples/tiny.tig",
            "examples/tinyrec.tig",
            "examples/tinytiny.tig",
//            "examples/type-dec.tig",                // excluded because of semantic error
            "examples/verytiny.tig",
            "examples/while-break.tig",
            "examples/while.tig",

//            "microbenchmarks/branching.tig",        // excluded because of semantic error
//            "microbenchmarks/break-intensive.tig",  // excluded because of semantic error
//            "microbenchmarks/list.tig",             // excluded because of semantic error
//            "microbenchmarks/permute.tig",          // excluded because of semantic error
//            "microbenchmarks/queens-looped.tig",    // excluded because of semantic error
//            "microbenchmarks/queens2.tig",          // excluded because of semantic error
//            "microbenchmarks/sieve.tig",            // excluded because of semantic error
//            "microbenchmarks/towers.tig",           // excluded because of semantic error
//            "microbenchmarks/var-local.tig",        // excluded because of semantic error
//            "microbenchmarks/var-parent.tig",       // excluded because of semantic error
            "microbenchmarks/while-call.tig",
//            "microbenchmarks/while-loop.tig",       // excluded because of semantic error

            "natives/chr.tig",
            "natives/concat.tig",
            "natives/exit.tig",
            "natives/flush.tig",
            "natives/getchar.tig",
            "natives/not.tig",
            "natives/ord.tig",
            "natives/print.tig",
            "natives/size.tig",
            "natives/substring.tig",

            "tests/binding/binding01.tig",
//            "tests/binding/binding02.tig",          // excluded because of semantic error
//            "tests/binding/binding03.tig",          // excluded because of semantic error
//            "tests/binding/binding04.tig",          // excluded because of semantic error
//            "tests/binding/binding05.tig",          // excluded because of semantic error
//            "tests/binding/binding06.tig",          // excluded because of semantic error
//            "tests/binding/binding07.tig",          // excluded because of semantic error
            "tests/binding/binding08.tig",
//            "tests/binding/binding10.tig",          // excluded because of semantic error
            "tests/binding/bindings09.tig",
            "tests/binding/bindings11.tig",

            "tests/operators/operator-test01.tig",
            "tests/operators/operators-test02.tig",
            "tests/operators/operators-test03.tig",

            "tests/statements/stat-test01.tig",
            "tests/statements/stat-test02.tig",
            "tests/statements/stat-test03.tig",

            "xmpl/a.tig",
            "xmpl/arrays-tiny.tig",
            "xmpl/arrays.tig",
            "xmpl/aterm.tig",
            "xmpl/break.tig",
            "xmpl/eval-test1.tig",
            "xmpl/eval-test2.tig",
            "xmpl/even-odd.tig",
            "xmpl/extract.tig",
            "xmpl/fac.tig",
            "xmpl/for.tig",
            "xmpl/function.tig",
            "xmpl/let.tig",
            "xmpl/merge.tig",
            "xmpl/multi-arg.tig",
            "xmpl/mytest4.tig",
            "xmpl/mytest5.tig",
            "xmpl/nestedfunctions.tig",
            "xmpl/prettyprint.tig",
            "xmpl/queens.tig",
            "xmpl/rec1.tig",
            "xmpl/record.tig",
            "xmpl/renaming1.tig",
            "xmpl/seq.tig",
            "xmpl/trtest1.tig",
            "xmpl/trtest2.tig",
            "xmpl/trtest3.tig",
            "xmpl/trtest4.tig",
//            "xmpl/typecheck-error1.tig",            // excluded because of semantic error

//            "xmpl2/error1.tig",                     // excluded because of semantic error
//            "xmpl2/matrix.tig",                     // excluded because of semantic error
//            "xmpl2/mytest1.tig",                    // excluded because of semantic error
            "xmpl2/mytest10.tig",
            "xmpl2/mytest11.tig",
            "xmpl2/mytest12.tig",
            "xmpl2/mytest13.tig",
            "xmpl2/mytest14.tig",
            "xmpl2/mytest15.tig",
            "xmpl2/mytest16.tig",
//            "xmpl2/mytest2.tig",                    // excluded because of semantic error
//            "xmpl2/mytest3.tig",                    // excluded because of semantic error
            "xmpl2/mytest5.tig",
            "xmpl2/mytest6.tig",
            "xmpl2/mytest7.tig"
//            "xmpl2/pp-test1.tig",                   // excluded because of semantic error
//            "xmpl2/tiny1.tig",                      // excluded because of semantic error
        ).map(it -> tigerTest("/" + it)).collect(Collectors.toList());
    }

    private DynamicTest tigerTest(String expectedTermPath) {
        final String testPath = TESTPATH + "/tiger";
        final String inputPath = testPath + "/input.tig.aterm";
        final String specPath = testPath + "/tiger.stx.aterm";
        final String specName = "static-semantics";
        final String rootRuleName = "programOk";
        final String csvPath = TIGER_CSV_OUTPUT_PATH + expectedTermPath + CSV_EXT;
        return completenessTest(testPath, testPath + expectedTermPath + ATERM_EXT, inputPath, specPath, specName, csvPath, rootRuleName);
    }

}
