import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class AssemblyManager {

    public void fragmentsToFile (String[] args) throws Exception {
        // read in arguments
        String dirPath = args[1];
        String filePass = args[2];
        int n = 0; // n is found on read-in

        // instantiate dependency classes
        FileOperations fileOps = new FileOperations();
        Cryptographics crypto = new Cryptographics();
        BytePartitioner partitioner = new BytePartitioner();
        BytePadder padder = new BytePadder();
        PathParser parser = new PathParser();

        // create secret key
        String secretKey = new String(crypto.hash(filePass), "UTF8");
        secretKey = secretKey.substring(secretKey.length() - 16);

        ArrayList<String> potentialFrags = new ArrayList<>();
        // weed out by file extension
        File dir = new File(dirPath);
        File[] listing = dir.listFiles();
        if (dir.listFiles() != null) {
            for (File child : listing) {
                if(child.getName().endsWith(".frg")) {
                    // add file to potential fragments
                    potentialFrags.add(child.getPath());
                }
            }
        } else {
            System.out.println("NOT A DIRECTORY");
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }

        // map potential .frg files {HMAC : IV}
        Map<String, byte[]> hmacIVMap = new HashMap<>();

        // map potential IVs {HMAC : Payload}
        Map<String, byte[]> hmacPayloadMap = new HashMap<>();

        for (String path : potentialFrags) {
            byte[] frgBytes = fileOps.readInFile(path);

            byte[] frgHMAC = new byte[32]; //HMAC is statically sized
            System.arraycopy(frgBytes, frgBytes.length-32, frgHMAC, 0, 32);

            byte[] frgIV = new byte[16]; //IV is statically sized
            System.arraycopy(frgBytes, frgBytes.length-48, frgIV,0, 16);

            byte[] frgPayload = Arrays.copyOfRange(frgBytes, 0, frgBytes.length-48);

            String hmacKey = Arrays.toString(frgHMAC);
            hmacIVMap.put(hmacKey, frgIV);
            hmacPayloadMap.put(hmacKey, frgPayload);
        }

        // ***** AUTHENTICATION *****
        // loop through and generate hmacs, comparing until no comparison can be found
        int seqID = 0; // first sequenceID to look for
        String genHMAC = Arrays.toString(crypto.hash(secretKey.concat(Integer.toString(seqID))));
        ArrayList<byte[]> authorizedPayloads = new ArrayList<>();

        // fileIV
        byte[] fileIV = new byte[0];

        // set file IV
        if (hmacIVMap.get(genHMAC) != null) {
            fileIV = hmacIVMap.get(genHMAC);
        }

        AESEncrypter cipher = new AESEncrypter(secretKey, fileIV);

        // while there exists a fragment with the right HMAC and right IV...
        while (hmacPayloadMap.get(genHMAC) != null) {
            authorizedPayloads.add(hmacPayloadMap.get(genHMAC));
            // iterate sequenceID and generate corresponding HMAC to look for
            seqID++;
            genHMAC = Arrays.toString(crypto.hash(secretKey.concat(Integer.toString(seqID))));
            n++; // keep count of number of fragments successfully read in
        }
        // if no fragments found, return failure
        if (n == 0) {
            System.out.println("no authorized fragments!");
            return;
        }

        System.out.println("count of authorized fragments: " + authorizedPayloads.size());

        // ***** DECRYPTION *****
        ArrayList<byte[]> scrambledPayloads = new ArrayList<>();
        int dataSize = 0;
        for (int i = 0; i < authorizedPayloads.size(); i++) {
            byte[] encrPayload = authorizedPayloads.get(i);
            System.out.println("payload: " + Arrays.toString(encrPayload));
            byte[] decrPayload = cipher.decrypt(encrPayload);
            scrambledPayloads.add(decrPayload);
            dataSize += decrPayload.length;
        }

        // ***** CONCATENATION *****
        ByteArrayOutputStream scramStream = new ByteArrayOutputStream();
        for (int i = 0; i < scrambledPayloads.size(); i++) {
            byte[] currLoad = scrambledPayloads.get(i);
            scramStream.write(currLoad);
            System.out.println("currLoad: " + Arrays.toString(currLoad));
        }
        byte[] scrambledBytes = scramStream.toByteArray();

        // ***** UNSCRAMBLE *****
        int obfuscVal = filePass.length() + n;
        // todo DEBUG DEBUG CHANGE BACK TO UNSCRAMBLE
        byte[] unscramdBytes = scrambledBytes; //crypto.scrambleBytes(scrambledBytes, obfuscVal);
        // todo DEBUG DEBUG CHANGE BACK TO UNSCRAMBLE

        // ***** EXTRACT FILENAME FROM PAYLOAD *****
        // last 256 bytes of payload (padded)
        byte[] paddedInfoBytes = new byte[256];

        System.out.println(scrambledBytes.length);
        System.arraycopy(unscramdBytes,unscramdBytes.length-256,paddedInfoBytes,0,256);



        // write reassembled file to disk
        try {
            // generate random 8-character string for file output
            String name = "myreassembledfile.txt";
            String fullPath = "test0/";
            fullPath = fullPath.concat(name);
            fileOps.writeOutFile(fullPath, unscramdBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Assembly Successful");

    }

    public void filterFiles () {
        return;
    }

    public void authenticateShards () {
        return;
    }

    public void decryptShards () {
        return;
    }

    public void concatShards () {
        return;
    }

    public void exportFile () {
        return;
    }
}
