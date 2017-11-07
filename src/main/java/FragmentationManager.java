import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FragmentationManager {

    // master function for class
    public static void fileToFragments (String[] args) throws Exception {
        // read in arguments
        val filepath = args[1];
        val filePass = args[3];
        val n = Integer.parseInt(args[2]);

        // instantiate dependency classes
        val fileOps = new FileOperations();
        val crypto = new Cryptographics();
        val partitioner = new BytePartitioner();
        val padder = new BytePadder();
        val parser = new PathParser();

        // create secret key
        String secretKey = new String(crypto.hash(filePass), "UTF8");
        secretKey = secretKey.substring(secretKey.length() - 16);

        // generate file-specific AES cipher
        val aesCipher = new AESEncrypter(secretKey, new byte[0]);
        byte[] IV = aesCipher.getInitV();

        // start processing input file
        byte[] fileBytes = fileOps.readInFile(filepath);

        // store fileInfo for eventual reassembly in 256 byte padded array
        byte[] filename = parser.extractFilename(filepath).getBytes();
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
        byte[] scrambledData = crypto.scrambleBytes(compFileData);

        // partition the scrambled file data into payload byte arrays
        byte[][] payloads = partitioner.splitWithRemainder(scrambledData, n);

        // process each payload into a complete fragment, iterating by sequenceID
        Shard[] shards = new Shard[n];
        for (int seqID = 0; seqID < n; seqID++) {
            System.out.println(seqID);
            // encrypt payloads
            byte[] encrPayload = aesCipher.encrypt(payloads[seqID]);

            // generate and append HMAC
            byte[] hmac = crypto.hash(secretKey.concat(Integer.toString(seqID)));

            // store as shard
            Shard shard = new Shard(encrPayload, IV, hmac);
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