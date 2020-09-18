package mb.sdf3.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.pie.api.Supplier;
import mb.resource.fs.FSResource;
import mb.sdf3.spoofax.task.Sdf3AnalyzeMulti;
import mb.sdf3.spoofax.task.Sdf3ToSignature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToSignatureTest extends TestBase {
    @Test
    @Disabled("For some reason, SingleLineTemplate are not getting a type annotation on the AST, even though there is a Statix type rule for them")
    void testTask() throws Exception {
        final FSResource resource = createTextFile("module nested/a context-free syntax A = <A>", "a.sdf3");
        final Sdf3ToSignature taskDef = languageComponent.getSdf3ToSignature();
        try(final MixedSession session = newSession()) {
            final Supplier<? extends Result<Sdf3AnalyzeMulti.SingleFileOutput, ?>> supplier = singleFileAnalysisResultSupplier(resource);
            final Result<IStrategoTerm, ?> result = session.require(taskDef.createTask(supplier));
            assertTrue(result.isOk());
            final IStrategoTerm output = result.unwrap();
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "signatures/nested/a-sig"));
        }
    }
}
