package mb.spoofax.core.language;

import mb.common.editor.HoverResult;
import mb.common.editor.ReferenceResolutionResult;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.style.Styling;
import mb.common.token.Tokens;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.completions.common.CompletionResult;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.menu.MenuItem;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface LanguageInstance {
    String getId();

    String getDisplayName();

    SetView<String> getFileExtensions();


    Task<? extends Option<? extends Tokens<?>>> createTokenizeTask(ResourceKey resourceKey);

    Task<Option<Styling>> createStyleTask(ResourceKey file, @Nullable ResourcePath rootDirectoryHint);

    @Deprecated
    default Task<Option<Styling>> createStyleTask(ResourceKey file) {
        return createStyleTask(file, null);
    }

    /**
     * Creates a task that produces completions.
     *
     * @param resourceKey      the key of the resource in which completion is invoked
     * @param primarySelection the primary selection at which completion is invoked
     * @return a {@link CompletionResult}; or {@code null} when no completions could be generated
     */
    Task<@Nullable CompletionResult> createCompletionTask(ResourceKey resourceKey, Region primarySelection);


    Task<KeyedMessages> createCheckOneTask(ResourceKey file, @Nullable ResourcePath rootDirectoryHint);

    Task<KeyedMessages> createCheckTask(ResourcePath rootDirectory);


    Task<Option<ReferenceResolutionResult>> createResolveTask(ResourcePath rootDirectory, ResourceKey file, Region region);

    Task<Option<HoverResult>> createHoverTask(ResourcePath rootDirectory, ResourceKey file, Region region);


    CollectionView<CommandDef<?>> getCommandDefs();

    CollectionView<AutoCommandRequest<?>> getAutoCommandRequests();


    CliCommand getCliCommand();


    ListView<MenuItem> getMainMenuItems();

    ListView<MenuItem> getResourceContextMenuItems();

    ListView<MenuItem> getEditorContextMenuItems();
}
