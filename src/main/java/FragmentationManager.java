import lombok.SneakyThrows;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;

public class FragmentationManager {

    @SneakyThrows
    public static void fileToFragments (String[] args) {
        /** Constants and debug flags
         */
        val DEBUGGING = false;
        val FILE_EXTENSTION = ".frg";
        val DEBUG_PATH = "test0/";

        /** Read in passed (and already sanitized) arguments
         */
        val filepath = args[1];
        val filePass = args[3];
        val n = Integer.parseInt(args[2]);

        /** Generate secret key using file hash to be used as input in
         * creating the secret key.
         */
        String secretKey = new String(Cryptographics.hash(filePass), "UTF8");
        secretKey = secretKey.substring(secretKey.length() - 16);

        /** Put file data into PuzzleFile object for further processing
         */
        byte[] fileBytes = FileOperations.readInFile(filepath);
        PuzzleFile wholeFileObj = new PuzzleFile(fileBytes, secretKey);

        /** Obtain filename and put it in padded chunk.
         */
        byte[] filename = PathParser.extractFilename(filepath).getBytes();
        byte[] fileInfoChunk = buildFilenameChunk(filename);

        /** Compress the file data to minimize storage footprint
         */
        //wholeFileObj.compress();

        /** Append filename chunk to PuzzleFile
         */
        wholeFileObj.addChunk(fileInfoChunk);

        /** Scramble the PuzzleFile object
         */
        wholeFileObj.scramble();

        /** Split file into n equal-sized Payload objects
         */
        Payload[] payloads = wholeFileObj.splitIntoPayloads(n);

        /** Generate Shard objects from payloads
         */
        Shard[] shards = formShards(payloads, secretKey);

        /** Write shards to disk
         */
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

        /** Delete original File
         */
        File file = new File(filepath);
        if (!DEBUGGING) {
            if (!file.delete()) {
                System.out.println("Failed to delete the file");
            }
        }
        System.out.println("Fragmentation Successful");
    }

    /** Given a filename in the form of a byte array, pad in a 256
     * byte chunk (padded with 0s).
     *
     * @param filename
     * @return fileInfo
     */
    @SneakyThrows
    public static byte[] buildFilenameChunk (byte[] filename) {
        byte[] padding = new byte[256 - filename.length];
        ByteArrayOutputStream fileInfoStream = new ByteArrayOutputStream();
        fileInfoStream.write( padding );
        fileInfoStream.write( filename );
        byte[] fileInfo = fileInfoStream.toByteArray();
        return fileInfo;
    }

    @SneakyThrows
    public static Shard[] formShards (Payload[] payloads, String secretKey) {
        // create IV that will be constant for all shards
        val aesCipher = new AESEncrypter(secretKey, new byte[0]);
        IV iv = new IV(aesCipher.getInitV());

        // process each payload into a complete fragment, iterating by sequenceID
        int n = payloads.length;
        Shard[] shards = new Shard[n];
        for (int seqID = 0; seqID < n; seqID++) {
            Payload payload = payloads[seqID];

            // encrypt payload
            payload.encrypt(aesCipher);

            // create HMAC
            HMAC hmac = new HMAC(secretKey,seqID);

            // store as Shard = Payload + IV + HMAC
            Shard shard = new Shard(payload, iv, hmac);
            shards[seqID] = shard;
        }

        return shards;
    }
}