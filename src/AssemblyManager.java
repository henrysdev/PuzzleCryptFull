import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AssemblyManager {

    public static void fragmentsToFile (String[] args) throws Exception {
        // read in arguments
        String dirPath = args[1];
        String filePass = args[2];
        int n = 0; // n is found on read-in

        // These are all static. No need for instances.
        FileOperations fileOps = new FileOperations();
        BytePartitioner partitioner = new BytePartitioner();
        BytePadder padder = new BytePadder();
        PathParser parser = new PathParser();

        // create secret key
        val fileHash = new String(Cryptographics.hash(filePass), "UTF8");
        val secretKey = fileHash.substring(fileHash.length() - 16); //16 is a magic number.
        val fileIv = new FragmentContainer();

        // map HMACs to Payloads {HMAC : Payload}
        Map<String, byte[]> hmacPayloadMap = new HashMap<>();
        val dir = new File(dirPath);

        Arrays.stream(dir.listFiles())
            .filter(file -> file.getName().endsWith(".frg"))
            .map(obj -> obj.getName())
            .forEach(consumePath(fileIv, hmacPayloadMap));

        // ***** AUTHENTICATION *****
        // loop through and generate hmacs, comparing until no comparison can be found
        int seqID = 0; // first sequenceID to look for
        String genHMAC = Arrays.toString(Cryptographics.hash(secretKey.concat(Integer.toString(seqID))));
        ArrayList<byte[]> authorizedPayloads = new ArrayList<>();

        AESEncrypter cipher = new AESEncrypter(secretKey, fileIv.getValue());

        // while there exists a fragment with the right HMAC and right IV...
        while (hmacPayloadMap.get(genHMAC) != null) {
            // if the IV is not correct, remove that entry and try again
            authorizedPayloads.add(hmacPayloadMap.get(genHMAC));
            // iterate sequenceID and generate corresponding HMAC to look for
            seqID++;
            genHMAC = Arrays.toString(Cryptographics.hash(secretKey.concat(Integer.toString(seqID))));
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
        byte[] unscramdBytes = Cryptographics.scrambleBytes(scrambledBytes);


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

    private static FragmentContainer getHMAC(byte[] fragment){
        byte[] frgHMAC = new byte[32]; //HMAC is statically sized to 32 bytes
        System.arraycopy(fragment, fragment.length-32, frgHMAC, 0, 32);
        return new FragmentContainer(frgHMAC);
    }

    private static FragmentContainer getIV(byte[] fragment){
        byte[] frgIV = new byte[16]; //IV is statically sized to 16 bytes
        System.arraycopy(fragment, fragment.length-48, frgIV,0, 16);
        return new FragmentContainer(frgIV);
    }

    private static FragmentContainer getPayload(byte[] fragment){
        byte[] frgPayload = Arrays.copyOfRange(fragment, 0, fragment.length - 48);
        return new FragmentContainer(frgPayload);
    }

    @AllArgsConstructor
    private static class FragmentContainer{
        @Getter
        @Setter
        private byte[] value;
        public FragmentContainer(){
            value = new byte[0];
        }
        @Override
        public String toString(){
            return Arrays.toString(value);
        }
    }

    private static Consumer<String> consumePath(final FragmentContainer fileIV, Map<String, byte[]> hmacPayloadMap){
        return path -> {
            val fragment = FileOperations.readInFile(path);
            val frgHMAC = getHMAC(fragment);
            val frgIV = getIV(fragment);

            // sets the IV to the first fragments IV found
            if (fileIV.getValue().length == 0) {
                fileIV.setValue(frgIV.getValue());
            }

            if (Arrays.equals(fileIV.getValue(), frgIV.getValue())) {
                val payload = getPayload(fragment);
                hmacPayloadMap.put(frgHMAC.toString(), payload.getValue());
            }
            else
            {
                System.out.println("Fragment TOSSED for wrong IV");
            }
        };
    }





}
