import java.io.File;

public class PathParser {

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
