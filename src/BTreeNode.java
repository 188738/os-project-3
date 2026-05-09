public class BTreeNode {

    public long blockId;
    public long parentId;
    public long numKeys;

    public long[] keys;
    public long[] values;
    public long[] children;

    public BTreeNode() {
        keys = new long[19];
        values = new long[19];
        children = new long[20];

        numKeys = 0;
    }
}