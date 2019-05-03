package mb.tiger;

import mb.jsglr1.common.JSGLR1ParseTable;
import mb.jsglr1.common.JSGLR1ParseTableException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class TigerParseTable implements Serializable {
    final JSGLR1ParseTable parseTable;

    private TigerParseTable(JSGLR1ParseTable parseTable) {
        this.parseTable = parseTable;
    }

    public static TigerParseTable fromClassLoaderResources() throws JSGLR1ParseTableException, IOException {
        final String resource = "mb/tiger/sdf.tbl";
        try(final @Nullable InputStream inputStream = TigerParseTable.class.getClassLoader().getResourceAsStream(resource)) {
            if(inputStream == null) {
                throw new RuntimeException(
                    "Cannot create parse table; cannot find resource '" + resource + "' in classloader resources");
            }
            final JSGLR1ParseTable parseTable = JSGLR1ParseTable.fromStream(inputStream);
            return new TigerParseTable(parseTable);
        }
    }
}
