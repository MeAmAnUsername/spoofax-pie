package mb.spoofax.eclipse;

import dagger.Component;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.command.EnclosingCommandContextProvider;
import mb.spoofax.eclipse.editor.PartClosedCallback;
import mb.spoofax.eclipse.editor.ScopeManager;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.pie.WorkspaceUpdate;
import mb.spoofax.eclipse.util.ColorShare;
import mb.spoofax.eclipse.util.ResourceUtil;
import mb.spoofax.eclipse.util.StyleUtil;

@PlatformScope
@Component(
    modules = {

    },
    dependencies = {
        EclipseLoggerComponent.class,
        EclipseResourceServiceComponent.class
    }
)
public interface EclipsePlatformComponent extends PlatformComponent {
    PieRunner getPieRunner();

    ResourceUtil getResourceUtil();

    ColorShare getColorShare();

    StyleUtil getStyleUtil();

    ScopeManager getScopeManager();

    PartClosedCallback getPartClosedCallback();

    EnclosingCommandContextProvider getEnclosingCommandContextProvider();

    WorkspaceUpdate.Factory getWorkspaceUpdateFactory();


    default void init() {
        getPartClosedCallback().register();
    }

    @Override default void close() {
        getPartClosedCallback().unregister();
        getColorShare().dispose();
    }
}
