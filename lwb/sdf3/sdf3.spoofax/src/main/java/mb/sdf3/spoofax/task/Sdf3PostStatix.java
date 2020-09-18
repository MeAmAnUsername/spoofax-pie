package mb.sdf3.spoofax.task;

import mb.sdf3.spoofax.Sdf3Scope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3PostStatix extends AstStrategoTransformTaskDef {
    @Inject public Sdf3PostStatix(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "statix-post");
    }

    @Override public String getId() {
        return Sdf3PostStatix.class.getSimpleName();
    }
}
