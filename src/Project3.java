public class Project3 {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: missing command");
            return;
        }

        String command = args[0];

        switch (command) {
            case "create":
                System.out.println("Create command called");
                break;

            case "insert":
                System.out.println("Insert command called");
                break;

            case "search":
                System.out.println("Search command called");
                break;

            case "load":
                System.out.println("Load command called");
                break;

            case "print":
                System.out.println("Print command called");
                break;

            case "extract":
                System.out.println("Extract command called");
                break;

            default:
                System.out.println("Error: unknown command");
        }
    }
}