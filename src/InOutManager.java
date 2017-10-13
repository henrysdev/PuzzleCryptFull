import java.io.File;

public class InOutManager {
    // attributes
    public static DebugLogger logger;

    public InOutManager () {
        logger = new DebugLogger();
    }

    public static void main (String[] args) {
        if (validateArguments(args)) {
            System.out.println("Operation Successful");
        }
        else {
            System.out.println(logger.printLogs());
        }
    }

    private static boolean validateArguments (String[] args) {
        switch (args[0]) {
            case "fragment":
                // validate that args[1] = filepath (String), and that this file exists
                String filePath = args[1].replaceFirst("^~", System.getProperty("user.home"));
                File targetFile = new File(filePath);
                if (targetFile.exists() && !targetFile.isDirectory()) {
                    // validate that args[2] = n (int)
                    try {
                        int n = Integer.parseInt(args[2]);
                    }
                    catch (NumberFormatException invalidN) {
                        logger.log("invalid format for argument[3]: " + invalidN.getMessage());
                        return false;
                    }

                    // validate that args[3] = password (String) with 10+ characters
                    String enteredPass = args[3];
                    if (enteredPass.length() >= 10) {
                        return true;
                    }
                    else {
                        logger.log("invalid input for argument[3]: insufficient password complexity");
                        return false;
                    }

                }
                else {
                    logger.log("invalid input for argument[2]: file not found.");
                    return false;
                }


            case "assemble":
                // validate that args[1] = folderpath (String), and this this directory exists
                String dirPath = args[1].replaceFirst("^~", System.getProperty("user.home"));
                File targetDir = new File(dirPath);
                if (targetDir.exists() && targetDir.isDirectory()) {
                    // validate args[2] = passwordKey (String)
                    String enteredPass = args[2];
                    if (enteredPass.length() >= 10) {
                        return true;
                    }
                    else {
                        logger.log("invalid input for argument[2]: insufficient password complexity");
                        return false;
                    }
                }
                else {
                    logger.log("invalid input for argument[1]: directory not found");
                    return false;
                }


            default:
                // return false if first argument does not match any cases
                logger.log("invalid input for argument[0]: unrecognized command");
                return false;
        }
    }
}