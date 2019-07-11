package mb.spoofax.eclipse.menu;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.SelectionUtil;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import java.util.ArrayList;

public abstract class ContextMenu extends CompoundContributionItem implements IWorkbenchContribution {
    private final PieRunner pieRunner;

    private final LanguageComponent languageComponent;
    private final String natureId;
    private final String addNatureCommandId;
    private final String removeNatureCommandId;
    private final String observeCommandId;
    private final String unobserveCommandId;

    private @MonotonicNonNull IServiceLocator serviceLocator;


    public ContextMenu(
        LanguageComponent languageComponent,
        String natureId,
        String addNatureCommandId,
        String removeNatureCommandId,
        String observeCommandId,
        String unobserveCommandId
    ) {
        this.pieRunner = SpoofaxPlugin.getComponent().getPieRunner();

        this.languageComponent = languageComponent;
        this.natureId = natureId;
        this.addNatureCommandId = addNatureCommandId;
        this.removeNatureCommandId = removeNatureCommandId;
        this.observeCommandId = observeCommandId;
        this.unobserveCommandId = unobserveCommandId;
    }

    @Override public void initialize(@NonNull IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }


    @Override protected IContributionItem[] getContributionItems() {
        final @Nullable ISelection simpleSelection = SelectionUtil.getActiveSelection();
        if(!(simpleSelection instanceof IStructuredSelection)) {
            return new IContributionItem[0];
        }
        final IStructuredSelection selection = (IStructuredSelection) simpleSelection;

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final MenuManager langMenu = new MenuManager(languageInstance.getDisplayName());

        final ArrayList<IProject> projects = SelectionUtil.toProjects(selection);
        if(!projects.isEmpty()) {
            boolean addNature = false;
            boolean removeNature = false;
            for(IProject project : projects) {
                try {
                    if(!project.hasNature(natureId)) {
                        addNature = true;
                    } else {
                        removeNature = true;
                    }
                } catch(CoreException e) {
                    // Ignore
                }
            }
            if(addNature) {
                langMenu.add(new CommandContributionItem(
                    new CommandContributionItemParameter(serviceLocator, null, addNatureCommandId,
                        CommandContributionItem.STYLE_PUSH)));
            }
            if(removeNature) {
                langMenu.add(new CommandContributionItem(
                    new CommandContributionItemParameter(serviceLocator, null, removeNatureCommandId,
                        CommandContributionItem.STYLE_PUSH)));
            }
        }

        final ArrayList<IFile> files = SelectionUtil.toFiles(selection);
        if(!files.isEmpty()) {
            final ArrayList<IFile> observeFiles = new ArrayList<>();
            final ArrayList<IFile> unobserveFiles = new ArrayList<>();
            for(IFile file : files) {
                final @Nullable String fileExtension = file.getFileExtension();
                if(fileExtension == null || !languageInstance.getFileExtensions().contains(fileExtension)) continue;
                if(pieRunner.isCheckObserved(languageInstance, file)) {
                    unobserveFiles.add(file);
                } else {
                    observeFiles.add(file);
                }
            }
            if(!observeFiles.isEmpty() || !unobserveFiles.isEmpty()) {
                langMenu.add(new Separator());
            }
            if(!observeFiles.isEmpty()) {
                langMenu.add(new CommandContributionItem(
                    new CommandContributionItemParameter(serviceLocator, null, observeCommandId,
                        CommandContributionItem.STYLE_PUSH)));
            }
            if(!unobserveFiles.isEmpty()) {
                langMenu.add(new CommandContributionItem(
                    new CommandContributionItemParameter(serviceLocator, null, unobserveCommandId,
                        CommandContributionItem.STYLE_PUSH)));
            }
        }

        return new IContributionItem[]{langMenu};
    }
}
