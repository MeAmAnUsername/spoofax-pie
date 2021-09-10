package mb.tiger.spoofax.reusable;

import mb.completions.common.CompletionResult;
import mb.jsglr.common.JsglrParseException;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDefs;
import mb.resource.text.TextResource;
import mb.tiger.spoofax.TestBase;
import mb.tiger.spoofax.task.reusable.TigerCompleteTaskDef;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TigerCompleteTaskDefTest extends TestBase {

    @SuppressWarnings("ConstantConditions") @Test public void smokeTest() throws Exception {
        TaskDefs taskDefs = new MapTaskDefs(component.getTaskDefs());
        System.out.println("taskDefs:");
        taskDefs.getAllTaskDefs().forEach(taskDef -> System.out.println(taskDef.getId()));
        TigerParse parse = (TigerParse) taskDefs.getTaskDef("mb.tiger.spoofax.task.reusable.TigerParse");
        TigerCompleteTaskDef tigerCompleteTaskDef = (TigerCompleteTaskDef) taskDefs.getTaskDef("mb.tiger.spoofax.task.reusable.TigerCompleteTaskDef");

        final TextResource resource = textResourceRegistry.createResource("1 + 2", "a.tig");
        final Supplier<IStrategoTerm> term = parse.inputBuilder()
            .withFile(resource.key)
            .buildAstSupplier()
            .map(res -> {
                try {
                    return res.unwrap();
                } catch(JsglrParseException e) {
                    fail();
                }
                return null; // here to appease the Java Gods, even though this statement is unreachable
            });

        try(final MixedSession session = newSession()) {
            final CompletionResult result = session.require(tigerCompleteTaskDef.createTask(new TigerCompleteTaskDef.Input(term)));
            assertTrue(result.isComplete());
            assertEquals(2, result.getProposals().size());
        }
    }
}
