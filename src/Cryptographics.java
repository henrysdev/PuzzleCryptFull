import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.nio.ByteBuffer;

public class Cryptographics {

    public byte[] scrambleBytes (byte[] fileBytes) throws Exception {


        //byte[] fb = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        //fileBytes = fb;

        //   fragment ./test0/mydoc.txt 3 henryhenry

        //   assemble ./test0/ henryhenry

        int arrLen = fileBytes.length;
        int step = 1; // additional step for index of chunk swaps
        int chunkSize = 3;

        for (int i=0; i < arrLen/2; i++) {
            fileBytes[i] = rotateRight(fileBytes[i], 4);
            fileBytes[arrLen-1 - i] = rotateRight(fileBytes[arrLen-1 - i], 4);

            if (i == 0)
                continue;
            if (i % (chunkSize + step) == 0) {
                ByteBuffer buf = ByteBuffer.wrap(fileBytes);

                byte[] chunk = Arrays.copyOfRange(fileBytes, i-chunkSize,i);
                byte[] tempChunk = Arrays.copyOfRange(fileBytes, arrLen-i, arrLen-i+chunkSize);

                buf.position(i-chunkSize);
                buf.put(tempChunk);

                buf.position(arrLen-i);
                buf.put(chunk);

                fileBytes = buf.array();
            }
        }

        System.out.println("final: " + Arrays.toString(fileBytes));

        return fileBytes;
    }

    // bit shift logic borrowed from
    // https://stackoverflow.com/questions/19181411/circular-rotate-issue-with-rotate-left
    public static byte rotateRight(byte bits, int shift)
    {
        return (byte)(((bits & 0xff)  >>> shift) | ((bits & 0xff) << (8 - shift)));
    }

    public static byte rotateLeft(byte bits, int shift)
    {
        return (byte)(((bits & 0xff) << shift) | ((bits & 0xff) >>> (8 - shift)));
    }


    public byte[] hash (String message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashRes = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        return hashRes;
    }

    // generate a randomized block of characters of given length
    public byte[] randomBlock (int blockLen) {

        final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final int N = alphabet.length();

        Random r = new Random();

        byte[] block = new byte[blockLen];

        for (int i = 0; i < blockLen; i++) {
            block[i] = (byte) alphabet.charAt(r.nextInt(N));
        }

        return block;
    }
}
