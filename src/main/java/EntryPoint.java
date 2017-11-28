import lombok.SneakyThrows;

import java.io.File;

public class EntryPoint {
    @SneakyThrows
    public static void main (String[] args) {
        if (validateArguments(args)) {
            handleInput(args);
        }
        else {
            System.out.println("Operation failed");
        }
    }

    /** Validate passed in command line arguments for accepted use cases.
     * Currently accepted use cases:
     *
     * Fragment a File
     * 1 file-to-many files
     * fragment  /path/to/target/file  number_of_fragments[int]  file_password
     *
     * Assemble Fragments
     * many files-to-1 file
     * assemble  /path/to/directory/containing/fragments/  file_password
     *
     * @param args
     * @return boolean
     */
    private static boolean validateArguments (String[] args) {
        switch (args[0]) {
            case "fragment":
                // validate that args[1] = filepath (String), and that this file exists
                String filePath = args[1].replaceFirst("^~", System.getProperty("user.home"));
                File targetFile = new File(filePath);
                if (targetFile.exists() && !targetFile.isDirectory()) {
                    // validate that args[2] = n (int)
                    try {
                        Integer.parseInt(args[2]);
                    }
                    catch (NumberFormatException invalidN) {
                        System.out.println("invalid format for argument[2]: " + invalidN.getMessage());
                        return false;
                    }

                    // validate that args[3] = password (String) with 10+ characters
                    String enteredPass = args[3];
                    if (enteredPass.length() >= 10) {
                        return true;
                    }
                    else {
                        System.out.println("invalid input for argument[3]: insufficient password complexity");
                        return false;
                    }

                }
                else {
                    System.out.println("invalid input for argument[2]: file not found.");
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
                        System.out.println("invalid input for argument[2]: insufficient password complexity");
                        return false;
                    }
                }
                else {
                    System.out.println("invalid input for argument[1]: directory not found");
                    return false;
                }


            default:
                // return false if first argument does not match any cases
                System.out.println("invalid input for argument[0]: unrecognized command");
                return false;
        }
    }

    /** Given validated input, pass arguments to the correct process
     *
     * @param args
     * @throws Exception
     */
    @SneakyThrows
    public static void handleInput (String[] args) {
        if (args[0].equals("fragment")) {
            FragmentationManager.fileToFragments(args);
        } else if (args[0].equals("assemble")) {
            AssemblyManager.fragmentsToFile(args);
        }
        return;
    }
}
