import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Project3 {
    static final int BLOCK_SIZE = 512;
    static final String MAGIC = "4348PRJ3";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: missing command");
            return;
        }

        String command = args[0];

        switch (command) {
            case "create":
                if (args.length != 2) {
                    System.out.println("Error: usage: create <index_file>");
                    return;
                }
                createIndexFile(args[1]);
                break;
            case "insert":
                if (args.length != 4) {
                System.out.println("Error: usage: insert <file> <key> <value>");
                return;
    }

    long key = Long.parseLong(args[2]);
    long value = Long.parseLong(args[3]);

    insert(args[1], key, value);
    break;
            default:
                System.out.println("Error: command not implemented yet");
        }
    }

    public static void createIndexFile(String fileName) {
        File file = new File(fileName);

        if (file.exists()) {
            System.out.println("Error: file already exists");
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.writeBytes(MAGIC);  
            raf.writeLong(0);        
            raf.writeLong(1);        

            while (raf.length() < BLOCK_SIZE) {
                raf.writeByte(0);
            }

            System.out.println("Created index file: " + fileName);
        } catch (IOException e) {
            System.out.println("Error creating file");
        }
    }

    public static void insert(String fileName, long key, long value) {

    try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {

        long rootId = IndexFile.getRootId(raf);

        // Tree is empty
        if (rootId == 0) {

            BTreeNode root = new BTreeNode();

            root.blockId = 1;
            root.parentId = 0;
            root.numKeys = 1;

            root.keys[0] = key;
            root.values[0] = value;

            IndexFile.writeNode(raf, root);

            IndexFile.setRootId(raf, 1);
            IndexFile.setNextBlockId(raf, 2);

            System.out.println("Inserted into empty tree");
        }

    } catch (IOException e) {
        System.out.println("Error inserting key");
    }
}
}