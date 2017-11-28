import lombok.*;
import org.apache.commons.lang3.ArrayUtils;
import sun.security.provider.SHA;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AssemblyManager {

    /** Primary driving function for transforming a group of fragment files
     * back into a single (original) file.
     *
     * @param args
     */
    @SneakyThrows
    public static void fragmentsToFile (String[] args) {
        /* Constants and debug flags
         */
        val DEBUGGING = false;
        val FILE_EXTENSION = ".frg";
        val DEBUG_PATH = "test0/NEW";

        String PATH = "test0/";

        /* Read in passed (and already sanitized) arguments
         */
        String dirPath = args[1];
        String filePass = args[2];

        /* Generate secret key to be used. Obtain file hash to be used as input in
         * creating the secret key.
         */
        val fileHash = new String(CryptoUtils.hash(filePass), "UTF8");
        val secretKey = fileHash.substring(fileHash.length() - 16); /*16 is a magic number.*/

        /* Read in potential fragments and store in dynamic list
         */
        ArrayList<File> fragmentFiles = new ArrayList<>();
        val dir = new File(dirPath);
        for (File f : dir.listFiles()) {
            if (f.getPath().contains(FILE_EXTENSION) ) {
                fragmentFiles.add(f);
            }
        }
        
        /* Store shards (confirmed fragments of the same file) in dynamic list
         * by only allowing fragmentFiles of the same IV. IV is set to the first
         * IV that is encountered.
         */
        ArrayList<Shard> shards = new ArrayList<>();
        IV constIV = new IV(new byte[0]);
        for (int i = 0; i < fragmentFiles.size(); i++) {
            File frag = fragmentFiles.get(i);
            PuzzleFile fileFrag = new PuzzleFile(IOUtils.readInFile(frag.getPath()));
            int fSize = fileFrag.getSize();
            IV iv = new IV(fileFrag.getChunk(fSize-48,fSize-32));

            if (constIV.getValue().length == 0) {
                constIV = new IV(iv.getValue());
            }
            else if (!Arrays.equals(constIV.getValue(), iv.getValue())) {
                System.out.println("fragment TOSSED for wrong IV");
                fragmentFiles.remove(i);
                i--;
                continue;
            }
            Payload payload = new Payload(fileFrag.getChunk(0, fSize-48));
            HMAC hmac = new HMAC(fileFrag.getChunk(fSize-32,fSize));
            Shard foundShard = new Shard(payload, iv, hmac);
            shards.add(foundShard);
        }

        /* Shards are authenticated and sorted via their HMACs, then the
         * Payload (which is still encrypted) is extracted and returned
         * to be stored in a dynamic list.
         */
        Payload[] authenticatedPayloads = sortByHMAC(shards, secretKey);
        AESEncrypter cipher = new AESEncrypter(secretKey, constIV.getValue());

        /* Payloads are decrypted, resulting in unencrypted (yet still
         * partitioned and scrambled) Payloads.
         */
        ArrayList<Payload> scrambledPayloads = new ArrayList<>();
        for (int i = 0; i < authenticatedPayloads.length; i++) {
            authenticatedPayloads[i].decrypt(cipher);
            scrambledPayloads.add(authenticatedPayloads[i]);
        }

        /* All payloads, (already in order from HMAC processing) are appended
         * to one another with a byte stream to form the original PuzzleFile
         * object (data still scrambled)
         */
        ByteArrayOutputStream scramStream = new ByteArrayOutputStream();
        for (int i = 0; i < scrambledPayloads.size(); i++) {
            byte[] currLoad = scrambledPayloads.get(i).getValue();
            scramStream.write(currLoad);
        }
        PuzzleFile composedFile = new PuzzleFile(scramStream.toByteArray(),secretKey);

        /* Unscramble the data of the reproduced original file
         */
        composedFile.unscramble();

        /* Extract filename (fileInfo) chunk from the reproduced PuzzleFile
         * object. Accomplish this by iterating through the chunk starting
         * from the end and breaking once it reaches the padding.
         */
        int fileSize = composedFile.getSize();
        byte[] paddedInfoBytes = composedFile.getChunk(fileSize-256, fileSize);
        int i = 255;
        int infoStartIndex = 0;
        while (i > 0) {
            if (paddedInfoBytes[i] == 0) {
                infoStartIndex = i+1;
                break;
            }
            i--;
        }
        byte[] fnameBytes = Arrays.copyOfRange(paddedInfoBytes, infoStartIndex, 256);

        /* Set PuzzleFile equal to the remaining portion of the PuzzleFile
         * (no extra chunks).
         */
        composedFile = new PuzzleFile(composedFile.getChunk(0,fileSize-256),secretKey);

        /* Decompress the file data returning the original file data in its entirety.
         */
        //TODO fix decompression
        //composedFile.decompress();

        byte[] originalBytes = composedFile.toByteArray();

        /* Export assembled file to disk and delete used fragmentFiles.
         */
        try {
            String name = new String(fnameBytes);
            System.out.println(name);
            if (DEBUGGING) {
                PATH = DEBUG_PATH;
            }
            String fullPath = PATH;
            fullPath = fullPath.concat(name);
            IOUtils.writeOutFile(fullPath, originalBytes);
            if (!DEBUGGING) {
                for (File f : fragmentFiles) {
                    if(!f.delete()) {
                        System.out.println("failed to delete fragment " + f.getName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Assembly Successful");
    }


    /** Given a list of confirmed shards, maps HMAC->Shard for each shard,
     * then generates the HMAC being looked for (starts from 0, increments)
     * and compares against HMAC map. If a match is found, the Payload component
     * of the matching shard is extracted and added to a dynamic (and inherently
     * sorted) list of Payloads to be returned.
     *
     * @param shards
     * @param secretKey
     * @return sortedPayloads
     */
    @SneakyThrows
    public static Payload[] sortByHMAC (ArrayList<Shard> shards, String secretKey) {
        /* <HMAC:Shard> using String representation for key
         */
        Map<String, Shard> hmacShardMap = new HashMap<>();

        for (Shard s : shards) {
            hmacShardMap.put(s.getHmac().toString(),s);
        }

        int n = shards.size();
        HMAC[] generatedHMACs = new HMAC[n];
        Payload[] sortedPayloads = new Payload[n];

        for (int seqID = 0; seqID < n; seqID++) {
            /* generate HMAC cryptographically and use it to create a new HMAC object
             */
            generatedHMACs[seqID] = new HMAC(secretKey, seqID);
        }

        /* iterate through fragments and find corresponding HMACs
         */
        for (int i = 0; i < n; i++) {
            try {
                Payload retrievedPayload = hmacShardMap.get(generatedHMACs[i].toString()).getPayload();
                sortedPayloads[i] = retrievedPayload;
            }
            catch(NullPointerException npe) {
                System.out.println("HMAC ID FAILURE");
            }
        }
        return sortedPayloads;
    }
}