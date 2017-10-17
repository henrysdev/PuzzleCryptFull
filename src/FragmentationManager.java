import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FragmentationManager {

    private String secretKey;

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
        secretKey = new String(crypto.hash(filePass), "UTF8");
        secretKey = secretKey.substring(secretKey.length() - 16);

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
        byte[] scrambledData = crypto.scrambleBytes(compFileData, obfuscVal);

        // partition the scrambled file data into payload byte arrays
        byte[][] payloads = partitioner.splitWithRemainder(scrambledData, n);

        // process each payload into a complete fragment, iterating by sequenceID
        Shard[] shards = new Shard[n];
        for (int seqID = 0; seqID < n; seqID++) {
            System.out.println(seqID);
            // encrypt payloads
            byte[] encrPayload = crypto.encrypt(payloads[seqID], secretKey);

            // generate and append HMAC
            byte[] hmac = crypto.hash(secretKey.concat(Integer.toString(seqID)));

            // store as shard
            Shard shard = new Shard(encrPayload, hmac);
            shards[seqID] = shard;
        }

        // write shards to disk
        for (Shard s : shards) {
            try {
                // generate random string for file output
                String name = new String(crypto.randomBlock(8));
                name = name.concat(".frg");
                fileOps.writeOutFile(name, s.toFragment());
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