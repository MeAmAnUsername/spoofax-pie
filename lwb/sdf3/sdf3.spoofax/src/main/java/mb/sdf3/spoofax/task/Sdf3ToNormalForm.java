package mb.sdf3.spoofax.task;

import mb.sdf3.spoofax.Sdf3Scope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ToNormalForm extends AstStrategoTransformTaskDef {
    @Inject public Sdf3ToNormalForm(Provider<StrategoRuntime> strategoRuntimeProvider) {
        super(strategoRuntimeProvider, "module-to-normal-form");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
