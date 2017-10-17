import java.io.IOException;
import java.util.Random;
import java.util.Arrays;

public class FragmentationManager {

    // master function for class
    public void fileToFragments (String[] args) {
        // read in arguments
        String filepath = args[1], filePass = args[3];
        int n = Integer.parseInt(args[2]);

        // instantiate dependency classes
        FileOperations fileOps = new FileOperations();
        Cryptographics crypto = new Cryptographics();

        // start processing input file
        byte[] fileBytes = fileOps.readInFile(filepath);
        int obfuscVal = n + filePass.length();
        fileBytes = crypto.scrambleBytes(fileBytes, obfuscVal);

        // partition file into byte array
        partitionBytes(fileBytes, n);

        /*
        Shard[] shards = new Shard[n];
        for (int i = 0; i < n; i++) {
            shards[i] = new Shard( );
        }
        */

        // write fragments to disk
        try {
            fileOps.writeOutFile(filepath, fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void partitionBytes (byte[] fileBytes, int n) {
        int remainder = fileBytes.length % n;
        int fragSize = ((fileBytes.length - (remainder)) / n);

        // random index
        boolean remainderFlag = false;
        int ri = 0;
        if (remainder != 0) {
            remainderFlag = true;
            Random rand = new Random();
            ri = rand.nextInt(n - 0);
        }

        byte[] payload = Arrays.copyOfRange(fileBytes, 0, fragSize + 1);
        Shard = new Shard(payload, 0);
        for (int i = 1; i < n; i++) {
            payload = Arrays.copyOfRange(fileBytes, (i)*fragSize, (i+1)*fragSize + 1);
            Shard s = new Shard(payload);
        }

        return;
    }

    public void encryptShards () {
        return;
    }

    public void authenticateShards () {
        return;
    }

    public void getShards () {
        return;
    }
}
