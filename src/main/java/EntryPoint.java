import lombok.SneakyThrows;

import java.io.File;

public class EntryPoint {
    @SneakyThrows
    public static void main (String[] args) {
        String response = validateArguments(args);
        if (response.equals("fragment") || response.equals("assemble")) {
            handleInput(response, args);
        }
        else {
            System.out.println(response);
            System.exit(0);
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
    private static String validateArguments (String[] args) {
        switch (args[0]) {
            case "fragment":
                /* validate that args[1] = filepath (String), and that this file exists
                 */
                String filePath = args[1].replaceFirst("^~", System.getProperty("user.home"));
                File targetFile = new File(filePath);
                if (targetFile.exists() && !targetFile.isDirectory()) {
                    /* validate that args[2] = n (int)
                     */
                    try {
                        Integer.parseInt(args[2]);
                    }
                    catch (NumberFormatException invalidN) {
                        return "invalid format for argument[2]: " + invalidN.getMessage();
                    }

                    /* validate that args[3] = password (String) with 10+ characters
                     */
                    String enteredPass = args[3];
                    if (enteredPass.length() >= 10) {
                        return "fragment";
                    }
                    else {
                        return "invalid input for argument[3]: insufficient password complexity";
                    }

                }
                else {
                    return "invalid input for argument[2]: file not found.";
                }


            case "assemble":
                /* validate that args[1] = folderpath (String), and this this directory exists
                 */
                String dirPath = args[1].replaceFirst("^~", System.getProperty("user.home"));
                File targetDir = new File(dirPath);
                if (targetDir.exists() && targetDir.isDirectory()) {
                    /* validate args[2] = passwordKey (String)
                     */
                    String enteredPass = args[2];
                    if (enteredPass.length() >= 10) {
                        return "assemble";
                    }
                    else {
                        return "invalid input for argument[2]: insufficient password complexity";
                    }
                }
                else {
                    return "invalid input for argument[1]: directory not found";
                }

            /* return invalid if first argument does not match any use cases
             */
            default:

                return "invalid input for argument[0]: unrecognized command";
        }
    }

    /** Given validated input, pass arguments to the correct process
     *
     * @param args
     * @throws Exception
     */
    @SneakyThrows
    public static void handleInput (String processName, String[] args) {
        if (processName.equals("fragment")) {
            FragmentationManager.fileToFragments(args);
        }
        else if (processName.equals("assemble")) {
            AssemblyManager.fragmentsToFile(args);
        }
    }
}
