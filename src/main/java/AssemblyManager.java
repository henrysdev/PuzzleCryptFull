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

    @SneakyThrows
    public static void fragmentsToFile (String[] args) {
        // constants
        val DEBUGGING = false;
        val FILE_EXTENSION = ".frg";
        String PATH = "test0/";
        if (DEBUGGING) {
            PATH = "test0/NEW";
        }

        // read in arguments
        String dirPath = args[1];
        String filePass = args[2];

        // create secret key
        val fileHash = new String(Cryptographics.hash(filePass), "UTF8");
        val secretKey = fileHash.substring(fileHash.length() - 16); //16 is a magic number.
        /** Tyler's Logic
        val fileIv = new FragmentContainer();
        // map HMACs to Payloads {HMAC : Payload}
        Map<String, byte[]> hmacPayloadMap = new HashMap<>();
        */
        val dir = new File(dirPath);

        // [BEGIN] HENRYS LOGIC
        ArrayList<File> frags = new ArrayList<>();
        for (File f : dir.listFiles()) {
            if (f.getPath().contains(FILE_EXTENSION) ) {
                frags.add(f);
            }
        }
        IV constIV = new IV(new byte[0]);
        ArrayList<Shard> shards = new ArrayList<>();
        for (File f : frags) {
            //TODO try/catch blocks for reading in file
            PuzzleFile fileFrag = new PuzzleFile(FileOperations.readInFile(f.getPath()));
            int fSize = fileFrag.getSize();
            IV iv = new IV(fileFrag.getChunk(fSize-48,fSize-32));

            if (constIV.getValue().length == 0) {
                constIV = new IV(iv.getValue());
            }
            else if (!Arrays.equals(constIV.getValue(), iv.getValue())) {
                System.out.println("fragment TOSSED for wrong IV");
                continue;
            }
            Payload payload = new Payload(fileFrag.getChunk(0, fSize-48));
            HMAC hmac = new HMAC(fileFrag.getChunk(fSize-32,fSize));
            Shard foundShard = new Shard(payload, iv, hmac);
            shards.add(foundShard);
        }

        // AUTHENTICATION
        Payload[] authorizedPayloads = sortByHMAC(shards, secretKey);
        AESEncrypter cipher = new AESEncrypter(secretKey, constIV.getValue());
        // [END] HENRYS LOGIC


        /** Tyler's Logic
        Arrays.stream(dir.listFiles())
            .filter(file -> file.getName().endsWith(FILE_EXTENSION))
            .map(obj -> obj.getName())
            .forEach(consumePath(fileIv, hmacPayloadMap));

        AESEncrypter cipher = new AESEncrypter(secretKey, fileIv.getValue());
        ArrayList<byte[]> authorizedPayloads = authOld(hmacPayloadMap, secretKey);

        ArrayList<byte[]> scrambledPayloads = new ArrayList<>();
        for (int i = 0; i < authorizedPayloads.size(); i++) {
            byte[] encrPayload = authorizedPayloads.get(i);
            byte[] decrPayload = cipher.decrypt(encrPayload);
            scrambledPayloads.add(decrPayload);
        }
        */

        // decrypt each payload
        ArrayList<Payload> scrambledPayloads = new ArrayList<>();
        for (int i = 0; i < authorizedPayloads.length; i++) {
            authorizedPayloads[i].decrypt(cipher);
            scrambledPayloads.add(authorizedPayloads[i]);
        }

        // chain together all cleartext payloads
        ByteArrayOutputStream scramStream = new ByteArrayOutputStream();
        for (int i = 0; i < scrambledPayloads.size(); i++) {
            byte[] currLoad = scrambledPayloads.get(i).getValue();
            scramStream.write(currLoad);
        }

        // compose stream into a file object
        PuzzleFile composedFile = new PuzzleFile(scramStream.toByteArray());

        // unscramble file
        composedFile.scramble();

        // extract filename from padded 256 byte chunk at end of payload
        int fileSize = composedFile.getSize();
        byte[] paddedInfoBytes = composedFile.getChunk(fileSize-256, fileSize);
        // finding filename: start at end and decrement until beginning of filename string is located
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

        // copy the rest the composed file back in to get back original file
        composedFile = new PuzzleFile(composedFile.getChunk(0,fileSize-256));

        // decompress the file
        //composedFile.decompress();
        //System.out.println("reassem filesize = " + composedFile.getSize());

        // cast to byte representation for writing to disk
        byte[] originalBytes = composedFile.toByteArray();

        // write reassembled file to disk
        try {
            // generate random 8-character string for file output
            String name = new String(fnameBytes);
            System.out.println(name);
            String fullPath = PATH;
            fullPath = fullPath.concat(name);
            FileOperations.writeOutFile(fullPath, originalBytes);
            if (!DEBUGGING) {
                for (File f : frags) {
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


    @SneakyThrows
    public static Payload[] sortByHMAC (ArrayList<Shard> shards, String secretKey) {
        // <HMAC:Shard> using String representation for key
        Map<String, Shard> hmacShardMap = new HashMap<>();

        for (Shard s : shards) {
            hmacShardMap.put(s.getHmac().toString(),s);
        }

        int n = shards.size();
        HMAC[] generatedHMACs = new HMAC[n];
        Payload[] sortedPayloads = new Payload[n];

        for (int seqID = 0; seqID < n; seqID++) {
            // generate HMAC cryptographically and use it to create a new HMAC object
            generatedHMACs[seqID] = new HMAC(Cryptographics.hash(secretKey.concat(Integer.toString(seqID))));
        }

        // iterate through fragments and find corresponding HMACs
        for (int i = 0; i < n; i++) {
            try {
                Payload retrievedPayload = hmacShardMap.get( generatedHMACs[i].toString() ).getPayload();
                sortedPayloads[i] = retrievedPayload;
            }
            catch(NullPointerException npe) {
                System.out.println("HMAC ID FAILURE");
            }
        }
        return sortedPayloads;
    }




/** Tyler's Logic
    @SneakyThrows
    public static ArrayList<byte[]> authOld (Map<String,byte[]> hmacPayloadMap, String secretKey) {
        // loop through and generate hmacs, comparing until no comparison can be found
        int seqID = 0; // first sequenceID to look for
        String genHMAC = Arrays.toString(Cryptographics.hash(secretKey.concat(Integer.toString(seqID))));
        ArrayList<byte[]> authorizedPayloads = new ArrayList<>();

        // while there exists a fragment with the right HMAC and right IV...
        while (hmacPayloadMap.get(genHMAC) != null) {
            // if the IV is not correct, remove that entry and try again
            authorizedPayloads.add(hmacPayloadMap.get(genHMAC));
            // iterate sequenceID and generate corresponding HMAC to look for
            seqID++;
            genHMAC = Arrays.toString(Cryptographics.hash(secretKey.concat(Integer.toString(seqID))));
        }
        // if no fragments found, return failure
        if (seqID == 0) {
            System.out.println("no authorized fragments!");
            return authorizedPayloads;
        }

        System.out.println("count of authorized fragments: " + authorizedPayloads.size());
        return authorizedPayloads;
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
            val fragment = FileOperations.readInFile("./test0/".concat(path));
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
*/
}