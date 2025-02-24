package mb.tiger;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.resource.classloader.NoopClassLoaderUrlResolver;
import mb.spoofax.test.SingleBaseLanguageTestBase;

class TestBase extends SingleBaseLanguageTestBase {
    public static final TigerClassloaderResources classloaderResources = new TigerClassloaderResources(new NoopClassLoaderUrlResolver());

    public TestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            () -> classloaderResources.resourceRegistry,
            () -> classloaderResources.definitionDirectory,
            (loggerFactory, definitionDirectory) -> new TigerParserFactory(definitionDirectory).create(),
            "Module",
            (loggerFactory, definitionDirectory) -> new TigerStylerFactory(loggerFactory, definitionDirectory).create(),
            (loggerFactory, resourceService, definitionDirectory) -> new TigerStrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDirectory).create(),
            resourceService -> new TigerConstraintAnalyzerFactory(resourceService).create(),
            false
        );
    }
}
