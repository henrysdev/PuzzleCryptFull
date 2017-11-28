import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtils {
    /** Try to read file from given path and return as byte array
     *
     * @param filepath
     * @return
     */
    public static byte[] readInFile(String filepath) {

        byte[] data;
        Path path = Paths.get(filepath);
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            data = new byte[0];
        }
        return data;
    }

    /** Given a target filepath and data to write to said file,
     * try to write file data to disk at this location.
     *
     * @param filepath
     * @param outputData
     * @throws IOException
     */
    public static void writeOutFile(String filepath, byte[] outputData) throws IOException {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(filepath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            stream.write(outputData);
        } finally {
            stream.close();
        }
    }

    /** Given a path to a file, extract just the filename
     *
     * @param path
     * @return filename
     * @throws Exception
     */
    public static String extractFilename (String path) throws Exception {
        File f = new File(path);
        return f.getName();
    }
}
