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

        // map potential .frg files {HMAC : Payload}
        Map<String, byte[]> hmacMap = new HashMap<>();

        for (String path : potentialFrags) {
            System.out.println("Potential fragment: " + path);
            byte[] frgBytes = fileOps.readInFile(path);

            System.out.println("File Bytes: " + Arrays.toString(frgBytes));

            byte[] frgHMAC = new byte[32]; //HMAC is statically sized
            System.arraycopy(frgBytes, frgBytes.length-32, frgHMAC, 0, 32);

            byte[] frgPayload = Arrays.copyOfRange(frgBytes, 0, frgBytes.length-32);
            System.out.println("HMAC: " + Arrays.toString(frgHMAC));
            System.out.println("Payload: " + Arrays.toString(frgPayload));

            hmacMap.put(Arrays.toString(frgHMAC), frgPayload);
        }

        // AUTHENTICATION
        // loop through and generate hmacs, comparing until no comparison can be found
        int seqID = 0; // first sequenceID to look for
        String genHMAC = Arrays.toString(crypto.hash( secretKey.concat(Integer.toString(seqID)) ) );

        ArrayList<byte[]> authorizedPayloads = new ArrayList<>();

        while (hmacMap.get(genHMAC) != null) {
            authorizedPayloads.add(hmacMap.get(genHMAC));
            // iterate sequenceID and generate corresponding HMAC to look for
            seqID++;
            genHMAC = Arrays.toString(crypto.hash( secretKey.concat(Integer.toString(seqID)) ) );
            n++;
        }

        if (n == 0) {
            System.out.println("no authorized fragments!");
        }

        for(byte[] authPload : authorizedPayloads) {
            System.out.println(authPload.toString());
        }

        // DECRYPTION
        byte[] foundIV = new byte[15]; //READ IN IV FROM FIRST 16bytes OF FRAGMENT
        ArrayList<byte[]> scrambledPayloads = new ArrayList<>();
        int dataSize = 0;
        for (byte[] encrPayload : authorizedPayloads) {
            AESEncrypter e1 = new AESEncrypter(secretKey, foundIV);
            byte[] decrPayload = e1.decrypt(encrPayload);
            scrambledPayloads.add(decrPayload);
            dataSize += decrPayload.length;
        }

        // CONCATENATATION
        ByteArrayOutputStream scramStream = new ByteArrayOutputStream();
        for (int i = 0; i < scrambledPayloads.size(); i++) {
            byte[] currLoad = scrambledPayloads.get(i);
            scramStream.write(currLoad);
            System.out.println("currLoad: " + Arrays.toString(currLoad));
        }
        byte[] scrambledBytes = scramStream.toByteArray();

        //System.out.println("ConcattedBytes: " + Arrays.toString(scrambledBytes) );

        // UNSCRAMBLE
        int obfuscVal = filePass.length() + n;
        // DEBUG DEBUG CHANGE BACK TO UNSCRAMBLE
        byte[] unscramdBytes = scrambledBytes; //crypto.scrambleBytes(scrambledBytes, obfuscVal);
        // DEBUG DEBUG CHANGE BACK TO UNSCRAMBLE

        // EXTRACT FILENAME
        // last 256 bytes
        byte[] paddedInfoBytes = new byte[256];

        System.out.println(scrambledBytes.length);
        System.arraycopy(unscramdBytes,unscramdBytes.length-256,paddedInfoBytes,0,256);



        // write shards to disk
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
