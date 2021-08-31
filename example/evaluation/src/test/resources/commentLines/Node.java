import java.util.List;

/**
 * A Node. Has a value and zero or more subnodes.
 */
public class Node {
    private Node[] subnodes;
    private int value;

    /**
     * Create a Node with the given value and subnodes.
     * @param value The value of the node.
     * @param subnodes The subnodes of the node.
     */
    public Node(int value, Node... subnodes) {
        this.subnodes = subnodes;
        this.value = value;
    }
}
