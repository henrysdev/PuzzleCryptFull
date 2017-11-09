import lombok.SneakyThrows;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;

public class FragmentationManager {

    // master function for class
    @SneakyThrows
    public static void fileToFragments (String[] args) {
        // constants
        val DEBUGGING = false;
        val FILE_EXTENSTION = ".frg";
        val DEBUG_PATH = "test0/";

        // read in arguments
        val filepath = args[1];
        val filePass = args[3];
        val n = Integer.parseInt(args[2]);

        // create secret key
        String secretKey = new String(Cryptographics.hash(filePass), "UTF8");
        secretKey = secretKey.substring(secretKey.length() - 16);

        // start processing input file
        byte[] fileBytes = FileOperations.readInFile(filepath);
        PuzzleFile wholeFileObj = new PuzzleFile(fileBytes, secretKey);

        // store fileInfo for eventual reassembly in 256 byte padded array
        byte[] filename = PathParser.extractFilename(filepath).getBytes();
        byte[] fileInfoChunk = buildFilenameChunk(filename);

        // compress file data
        //System.out.println("orig filesize = " + wholeFileObj.getSize());
        //wholeFileObj.compress();

        // add fileInfo chunk to PuzzleFile obj
        wholeFileObj.addChunk(fileInfoChunk);

        // scramble file data
        wholeFileObj.scramble();

        // shatter file into shards
        Shard[] shards = wholeFileObj.toShards(n);

        // write shards to disk
        for (Shard s : shards) {
            try {
                // generate random 8-character string for file output
                String name = new String(Cryptographics.randomBlock(8));
                name = name.concat(FILE_EXTENSTION);
                String fullPath = DEBUG_PATH;
                fullPath = fullPath.concat(name);
                FileOperations.writeOutFile(fullPath, s.toFragment());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // delete original file
        File file = new File(filepath);
        if (!DEBUGGING) {
            if (!file.delete()) {
                System.out.println("Failed to delete the file");
            }
        }
        System.out.println("Fragmentation Successful");
    }

    @SneakyThrows
    public static byte[] buildFilenameChunk (byte[] filename) {
        byte[] padding = new byte[256 - filename.length];
        ByteArrayOutputStream fileInfoStream = new ByteArrayOutputStream();
        fileInfoStream.write( padding );
        fileInfoStream.write( filename );
        byte[] fileInfo = fileInfoStream.toByteArray();
        return fileInfo;
    }
}