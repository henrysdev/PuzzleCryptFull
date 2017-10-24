import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class FragmentationManager {

    // master function for class
    public void fileToFragments (String[] args) throws Exception {
        // read in arguments
        String filepath = args[1];
        String filePass = args[3];
        int n = Integer.parseInt(args[2]);

        // instantiate dependency classes
        FileOperations fileOps = new FileOperations();
        Cryptographics crypto = new Cryptographics();
        BytePartitioner partitioner = new BytePartitioner();
        BytePadder padder = new BytePadder();
        PathParser parser = new PathParser();

        // create secret key
        String secretKey = new String(crypto.hash(filePass), "UTF8");
        secretKey = secretKey.substring(secretKey.length() - 16);

        // generate file-specific AES cipher
        AESEncrypter aesCipher = new AESEncrypter(secretKey, new byte[0]);
        byte[] IV = aesCipher.getInitVect();

        // start processing input file
        byte[] fileBytes = fileOps.readInFile(filepath);
        int obfuscVal = n + filePass.length();

        // store fileInfo for eventual reassembly in 256 byte padded array
        byte[] filename = "myreallycoolfile.txt".getBytes();
        byte[] padding = new byte[256 - filename.length];
        ByteArrayOutputStream fileInfoStream = new ByteArrayOutputStream();
        fileInfoStream.write( padding );
        fileInfoStream.write( filename );
        byte[] fileInfo = fileInfoStream.toByteArray();

        // append file info to file data to form complete file data
        ByteArrayOutputStream compFileDataStream = new ByteArrayOutputStream();
        compFileDataStream.write(fileBytes);
        compFileDataStream.write(fileInfo);
        byte[] compFileData = compFileDataStream.toByteArray();

        // scramble bytes of payload
        // DEBUG DEBUG CHANGE BACK TO SCRAMBLE
        byte[] scrambledData = compFileData;//crypto.scrambleBytes(compFileData, obfuscVal);
        // DEBUG DEBUG CHANGE BACK TO SCRAMBLE

        // partition the scrambled file data into payload byte arrays
        byte[][] payloads = partitioner.splitWithRemainder(scrambledData, n);

        // DEBUGGING LOGIC
        byte[] myBytes = {(byte) 0xAA, (byte) 0x9C, (byte) 0xA3, (byte) 0x67,
                (byte) 0x67, (byte) 0x7A, (byte) 0xE9, (byte) 0x12, (byte) 0xAA, (byte) 0x9C, (byte) 0xA3, (byte) 0x67,
                (byte) 0x67, (byte) 0x7A, (byte) 0xE9, (byte) 0x12, (byte) 0xAA, (byte) 0x9C, (byte) 0xA3, (byte) 0x67,
                (byte) 0x67, (byte) 0x7A, (byte) 0xE9, (byte) 0x12, (byte) 0xAA, (byte) 0x9C, (byte) 0xA3, (byte) 0x67,
                (byte) 0x67, (byte) 0x7A, (byte) 0xE9, (byte) 0x12};
        byte[][] tps = partitioner.splitWithRemainder(myBytes, 3);
        for (int i = 0; i < tps.length; i++) {
            System.out.println( " original: " + Arrays.toString(tps[i]) );

            AESEncrypter e1 = new AESEncrypter(secretKey, new byte[0]);
            byte[] eload = e1.encrypt(tps[i]); //crypto.encrypt(tps[i], secretKey);
            System.out.println( "encrypted: " + Arrays.toString(eload) );

            AESEncrypter d1 = new AESEncrypter(secretKey, e1.getInitVect());
            byte[] dload = d1.decrypt(eload);//crypto.decrypt(eload, secretKey, IV); //crypto.decrypt(eload, secretKey);
            System.out.println(" decrypted: " + Arrays.toString(dload) + "\n");
        }
        // /DEBUGGING LOGIC

        // process each payload into a complete fragment, iterating by sequenceID
        Shard[] shards = new Shard[n];
        for (int seqID = 0; seqID < n; seqID++) {
            System.out.println(seqID);
            // encrypt payloads
            byte[] encrPayload = aesCipher.encrypt(payloads[seqID]);

            // generate and append HMAC
            byte[] hmac = crypto.hash(secretKey.concat(Integer.toString(seqID)));

            // store as shard
            Shard shard = new Shard(encrPayload, hmac);
            shards[seqID] = shard;
        }



        // write shards to disk
        for (Shard s : shards) {
            try {
                // generate random 8-character string for file output
                String name = new String(crypto.randomBlock(8));
                name = name.concat(".frg");
                String fullPath = "test0/";
                fullPath = fullPath.concat(name);
                fileOps.writeOutFile(fullPath, s.toFragment());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //TODO flesh out way of compressing data before encrypting
    public void compressPayload () throws Exception {
        return;
    }
}