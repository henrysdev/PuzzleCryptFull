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

        byte[] fileIV = new byte[0];

        // map HMACs to Payloads {HMAC : Payload}
        Map<String, byte[]> hmacPayloadMap = new HashMap<>();

        for (String path : potentialFrags) {
            byte[] frgBytes = fileOps.readInFile(path);

            byte[] frgHMAC = new byte[32]; //HMAC is statically sized to 32 bytes
            System.arraycopy(frgBytes, frgBytes.length-32, frgHMAC, 0, 32);

            byte[] frgIV = new byte[16]; //IV is statically sized to 16 bytes
            System.arraycopy(frgBytes, frgBytes.length-48, frgIV,0, 16);
            // sets the IV to the first fragments IV found
            if (fileIV.length == 0) {
                fileIV = frgIV;
            }

            if (Arrays.equals(fileIV, frgIV)) {
                byte[] frgPayload = Arrays.copyOfRange(frgBytes, 0, frgBytes.length - 48);
                String hmacKey = Arrays.toString(frgHMAC); // cast to string to use as key for HashMap
                hmacPayloadMap.put(hmacKey, frgPayload);
            }
            else
            {
                System.out.println("Fragment TOSSED for wrong IV");
            }
        }

        // ***** AUTHENTICATION *****
        // loop through and generate hmacs, comparing until no comparison can be found
        int seqID = 0; // first sequenceID to look for
        String genHMAC = Arrays.toString(crypto.hash(secretKey.concat(Integer.toString(seqID))));
        ArrayList<byte[]> authorizedPayloads = new ArrayList<>();

        AESEncrypter cipher = new AESEncrypter(secretKey, fileIV);

        // while there exists a fragment with the right HMAC and right IV...
        while (hmacPayloadMap.get(genHMAC) != null) {
            // if the IV is not correct, remove that entry and try again
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
            byte[] decrPayload = cipher.decrypt(encrPayload);
            scrambledPayloads.add(decrPayload);
            dataSize += decrPayload.length;
        }

        // ***** CONCATENATION *****
        ByteArrayOutputStream scramStream = new ByteArrayOutputStream();
        for (int i = 0; i < scrambledPayloads.size(); i++) {
            byte[] currLoad = scrambledPayloads.get(i);
            scramStream.write(currLoad);
        }
        byte[] scrambledBytes = scramStream.toByteArray();

        // ***** UNSCRAMBLE *****
        byte[] unscramdBytes = crypto.scrambleBytes(scrambledBytes);


        // ***** EXTRACT FILENAME FROM PAYLOAD *****
        // last 256 bytes of payload (padded)
        byte[] paddedInfoBytes = Arrays.copyOfRange(unscramdBytes, unscramdBytes.length-256, unscramdBytes.length);
        byte[] origFile = Arrays.copyOfRange(unscramdBytes,0, unscramdBytes.length-256);

        // finding filename: scan until beginning of padding is located
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

        // write reassembled file to disk
        try {
            // generate random 8-character string for file output
            String name = new String(fnameBytes);
            System.out.println(name);
            String fullPath = "test0/NEWNEW";
            fullPath = fullPath.concat(name);
            fileOps.writeOutFile(fullPath, origFile);
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
