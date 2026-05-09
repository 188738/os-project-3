import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexFile {
    static final int BLOCK_SIZE = 512;

    public static void writeNode(RandomAccessFile raf, BTreeNode node) throws IOException {
        raf.seek(node.blockId * BLOCK_SIZE);

        raf.writeLong(node.blockId);
        raf.writeLong(node.parentId);
        raf.writeLong(node.numKeys);

        for (int i = 0; i < 19; i++) {
            raf.writeLong(node.keys[i]);
        }

        for (int i = 0; i < 19; i++) {
            raf.writeLong(node.values[i]);
        }

        for (int i = 0; i < 20; i++) {
            raf.writeLong(node.children[i]);
        }
    }

    public static BTreeNode readNode(RandomAccessFile raf, long blockId) throws IOException {
        raf.seek(blockId * BLOCK_SIZE);

        BTreeNode node = new BTreeNode();

        node.blockId = raf.readLong();
        node.parentId = raf.readLong();
        node.numKeys = raf.readLong();

        for (int i = 0; i < 19; i++) {
            node.keys[i] = raf.readLong();
        }

        for (int i = 0; i < 19; i++) {
            node.values[i] = raf.readLong();
        }

        for (int i = 0; i < 20; i++) {
            node.children[i] = raf.readLong();
        }

        return node;
    }
}