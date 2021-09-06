package mb.tiger.spoofax.task;

import mb.common.option.Option;
import mb.jsglr.common.JSGLRTokens;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.tiger.spoofax.TigerScope;
import mb.tiger.spoofax.task.reusable.TigerParse;

import javax.inject.Inject;
import java.io.IOException;

@TigerScope
public class TigerIdeTokenize implements TaskDef<ResourceKey, Option<JSGLRTokens>> {
    private final TigerParse parse;

    @Inject public TigerIdeTokenize(TigerParse parse) {
        this.parse = parse;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.TigerTokenize";
    }

    @Override
    public Option<JSGLRTokens> exec(ExecContext context, ResourceKey key) throws IOException {
        return context.require(parse.inputBuilder().withFile(key).buildTokensSupplier()).ok();
    }
}
