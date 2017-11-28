import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtils {
    // read file from path, return as byte array
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

    public static String extractFilename (String path) throws Exception {
        File f = new File(path);
        return f.getName();
    }

    public static String extractDirectory (String filepath) throws Exception {
        File f = new File(filepath);
        //Path p = Paths.get(yourFileNameUri);
        //Path folder = p.getParent();
        return "";
    }
}
