package mb.str.util;

import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.resource.hierarchical.walk.TrueResourceWalker;

public class StrategoUtil {
    public static ResourceWalker createResourceWalker() {
        return new TrueResourceWalker();
    }

    public static ResourceMatcher createResourceMatcher() {
        return new PathResourceMatcher(new ExtensionsPathMatcher("str"));
    }
}
