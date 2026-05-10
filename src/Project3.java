import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
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
            case "print":
                if (args.length != 2) {
                    System.out.println("Error: usage: print <file>");
                    return;
                }
                print(args[1]);
                break;
            case "search":
                if (args.length != 3) {
                    System.out.println("Error: usage: search <file> <key>");
                    return;
                }
                long searchKey = Long.parseLong(args[2]);
                search(args[1], searchKey);
                break;
            case "extract":
                if (args.length != 3) {
                    System.out.println("Error: usage: extract <index_file> <output_csv>");
                    return;
                }
                extract(args[1], args[2]);
                break;
            case "load":
                if (args.length != 3) {
                    System.out.println("Error: usage: load <index_file> <csv_file>");
                    return;
                }
                load(args[1], args[2]);
                break;
            default:
                System.out.println("Error: command not implemented yet");
        }
    }
    
    public static void extract(String fileName, String outputFileName) {
    File outputFile = new File(outputFileName);

    if (outputFile.exists()) {
        System.out.println("Error: output file already exists");
        return;
    }

    try (
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        PrintWriter writer = new PrintWriter(outputFile)
    ) {
        long rootId = IndexFile.getRootId(raf);

        if (rootId != 0) {
            extractNode(raf, rootId, writer);
        }

        System.out.println("Extracted to " + outputFileName);

    } catch (IOException e) {
        System.out.println("Error extracting file");
    }
}

public static void extractNode(RandomAccessFile raf, long blockId, PrintWriter writer) throws IOException {
    BTreeNode node = IndexFile.readNode(raf, blockId);

    for (int i = 0; i < node.numKeys; i++) {
        if (node.children[i] != 0) {
            extractNode(raf, node.children[i], writer);
        }

        writer.println(node.keys[i] + "," + node.values[i]);
    }

    if (node.children[(int) node.numKeys] != 0) {
        extractNode(raf, node.children[(int) node.numKeys], writer);
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
    public static void load(String fileName, String csvFileName) {
    try (BufferedReader br = new BufferedReader(new FileReader(csvFileName))) {
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");

            if (parts.length != 2) {
                System.out.println("Error: invalid csv line");
                return;
            }

            long key = Long.parseLong(parts[0].trim());
            long value = Long.parseLong(parts[1].trim());

            insert(fileName, key, value);
        }

        System.out.println("Load complete");

    } catch (IOException e) {
        System.out.println("Error loading csv file");
    }
}
    public static void insert(String fileName, long key, long value) {
    try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
        long rootId = IndexFile.getRootId(raf);

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

            System.out.println("Inserted");
            return;
        }

        BTreeNode root = IndexFile.readNode(raf, rootId);

        if (root.numKeys < 19) {
            insertIntoNonFullNode(root, key, value);
            IndexFile.writeNode(raf, root);
            System.out.println("Inserted");
            return;
        }

        long newRootId = IndexFile.getNextBlockId(raf);
        long rightChildId = newRootId + 1;

        BTreeNode newRoot = new BTreeNode();
        BTreeNode rightChild = new BTreeNode();

        newRoot.blockId = newRootId;
        newRoot.parentId = 0;
        newRoot.numKeys = 1;
        newRoot.keys[0] = root.keys[9];
        newRoot.values[0] = root.values[9];
        newRoot.children[0] = root.blockId;
        newRoot.children[1] = rightChildId;

        rightChild.blockId = rightChildId;
        rightChild.parentId = newRootId;
        rightChild.numKeys = 9;

        for (int i = 0; i < 9; i++) {
            rightChild.keys[i] = root.keys[i + 10];
            rightChild.values[i] = root.values[i + 10];
        }

        root.numKeys = 9;
        root.parentId = newRootId;

        IndexFile.writeNode(raf, root);
        IndexFile.writeNode(raf, rightChild);
        IndexFile.writeNode(raf, newRoot);

        IndexFile.setRootId(raf, newRootId);
        IndexFile.setNextBlockId(raf, rightChildId + 1);

        if (key < newRoot.keys[0]) {
            insertIntoNonFullNode(root, key, value);
            IndexFile.writeNode(raf, root);
        } else {
            insertIntoNonFullNode(rightChild, key, value);
            IndexFile.writeNode(raf, rightChild);
        }

        System.out.println("Inserted");

    } catch (IOException e) {
        System.out.println("Error inserting key");
    }
}

public static void insertIntoNonFullNode(BTreeNode node, long key, long value) {
    int i = (int) node.numKeys - 1;

    while (i >= 0 && key < node.keys[i]) {
        node.keys[i + 1] = node.keys[i];
        node.values[i + 1] = node.values[i];
        i--;
    }

    node.keys[i + 1] = key;
    node.values[i + 1] = value;
    node.numKeys++;
}

    public static void search(String fileName, long key) {
    try (RandomAccessFile raf = new RandomAccessFile(fileName, "r")) {
        long rootId = IndexFile.getRootId(raf);

        if (rootId == 0) {
            System.out.println("Error: key not found");
            return;
        }

        searchNode(raf, rootId, key);

    } catch (IOException e) {
        System.out.println("Error searching file");
    }
}

public static void searchNode(RandomAccessFile raf, long blockId, long key) throws IOException {
    BTreeNode node = IndexFile.readNode(raf, blockId);

    int i = 0;

    while (i < node.numKeys && key > node.keys[i]) {
        i++;
    }

    if (i < node.numKeys && key == node.keys[i]) {
        System.out.println(node.keys[i] + "," + node.values[i]);
        return;
    }

    if (node.children[i] == 0) {
        System.out.println("Error: key not found");
        return;
    }

    searchNode(raf, node.children[i], key);
}

    public static void print(String fileName) {
    try (RandomAccessFile raf = new RandomAccessFile(fileName, "r")) {
        long rootId = IndexFile.getRootId(raf);

        if (rootId == 0) {
            return;
        }

        printNode(raf, rootId);

    } catch (IOException e) {
        System.out.println("Error printing file");
    }
}

public static void printNode(RandomAccessFile raf, long blockId) throws IOException {
    BTreeNode node = IndexFile.readNode(raf, blockId);

    for (int i = 0; i < node.numKeys; i++) {
        if (node.children[i] != 0) {
            printNode(raf, node.children[i]);
        }

        System.out.println(node.keys[i] + "," + node.values[i]);
    }

    if (node.children[(int) node.numKeys] != 0) {
        printNode(raf, node.children[(int) node.numKeys]);
    }
}
}