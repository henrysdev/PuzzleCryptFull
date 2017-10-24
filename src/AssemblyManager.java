import java.io.File;
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
        System.out.println("Assembly Successful");

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

        // map potential .frg files {HMAC : Payload}
        Map<byte[], byte[]> hmacMap = new HashMap<>();

        for (String path : potentialFrags) {
            byte[] frgBytes = fileOps.readInFile(path);
            byte[] frgHMAC = new byte[64]; //HMAC is statically sized
            System.arraycopy(frgBytes, 0, frgHMAC, 0, 64);
            byte[] frgPayload = Arrays.copyOfRange(frgBytes, 0, frgBytes.length-64);
            hmacMap.put(frgHMAC, frgPayload);

            // DEBUGGING
            /*
            int i = 0;
            for (byte b : frgHMAC) {
                System.out.println((char) b);
                i++;
            }
            System.out.println(i);
            */
            //map.get(frgHMAC);
        }

        // AUTHENTICATION
        // loop through and generate hmacs, comparing until no comparison can be found
        int seqID = 0; // first sequenceID to look for
        byte[] genHMAC = crypto.hash( secretKey.concat(Integer.toString(seqID)) );
        ArrayList<byte[]> authorizedPayloads = new ArrayList<>();

        while (hmacMap.get(genHMAC) != null) {
            authorizedPayloads.add(hmacMap.get(genHMAC));
            // iterate sequenceID and generate corresponding HMAC to look for
            seqID++;
            genHMAC = crypto.hash( secretKey.concat(Integer.toString(seqID)) );
        }

        // DECRYPTION
        ArrayList<byte[]> scrambledPayloads = new ArrayList<>();
        int dataSize = 0;
        for (byte[] encrPayload : authorizedPayloads) {
            byte[] decrPayload = crypto.decrypt(encrPayload, secretKey);
            scrambledPayloads.add(decrPayload);
            dataSize += decrPayload.length;
        }

        // CONCATENATATION
        byte[] scrambledBytes = new byte[dataSize];
        int bytePos = 0;
        for (int i = 0; i < scrambledPayloads.size(); i++) {
            System.arraycopy(scrambledPayloads[i],0,scrambledBytes,bytePos);
            bytePos += scrambledBytes[i].length;
        }

        // UNSCRAMBLE



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
