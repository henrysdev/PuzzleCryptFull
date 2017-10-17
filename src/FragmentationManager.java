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
        Shard[] shards = partitionBytes(fileBytes, n);

        // write fragments to disk
        int i = 0;
        for (Shard s : shards) {
            try {
                fileOps.writeOutFile(filepath + Integer.toString(i), s.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public Shard[] partitionBytes (byte[] fileBytes, int n) {
        int remainder = fileBytes.length % n;
        int fragSize = ((fileBytes.length - (remainder)) / n);

        // random index
        boolean remainderFlag = false;
        int ri = 0;
        if (remainder != 0) {
            remainderFlag = true;
            Random rand = new Random();
            ri = rand.nextInt(n - 0);
            System.out.println(ri);
        }

        //byte[] payload = Arrays.copyOfRange(fileBytes, 0, fragSize + 1);
        // handle offset for largest byte
        int rmdrOffsetBeg, rmdrOffsetEnd = 0;
        Shard[] shards = new Shard[n];
        for (int i = 1; i < n+1; i++) {
            // if its the randomly selected fragment that will take on the remainder...
            if (ri == i - 1 ) {
                rmdrOffsetBeg = 0;
                rmdrOffsetEnd = remainder + 1;
            }
            // if last time it was the special frag...
            else if (ri == i - 2) {
                rmdrOffsetBeg = remainder;
                rmdrOffsetEnd = 1;
            }
            else {
                rmdrOffsetBeg = 0;
                rmdrOffsetEnd = 1;
            }
            byte[] payload = Arrays.copyOfRange(fileBytes, (i-1)*fragSize + rmdrOffsetBeg, (i)*fragSize + rmdrOffsetEnd + 1);
            Shard s = new Shard(payload);
            shards[i-1] = s;
        }
        return shards;
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
