import com.sun.tools.javac.util.ArrayUtils;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.nio.ByteBuffer;

public class Cryptographics {

    // TODO reduce complexity and efficiency
     /*
        int arrLen = fileBytes.length;
        byte[] scramBytes = new byte[arrLen];

        // reverse order of bytes
        for (int i=0; i < arrLen; i++) {
            scramBytes[i] = fileBytes[arrLen - i - 1];
        }

        // flip reversed bytes if the remainder of (i % obfuscVal) is 1
        for (int i=0; i < arrLen; i++) {
            if (i % obfuscVal == 1) {
                int iSwap = arrLen - i - 1;
                byte chosen = scramBytes[i];
                byte temp = scramBytes[iSwap];
                scramBytes[i] = temp;
                scramBytes[iSwap] = chosen;
            }
        }

        return scramBytes;
        */

    public byte[] scrambleBytes (byte[] fileBytes, int obfuscVal) throws Exception {

        /* DEBUGGING
        //byte[] fb = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        byte[] fb = {0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1};
        fileBytes = fb;
        */

        System.out.println("orig: " + Arrays.toString(fileBytes));

        int step = 2; // additional step for index of chunk swaps
        int chunkSize = 3; // number of bytes in a chunk
        int arrLen = fileBytes.length;

        for (int i=0; i < arrLen/2; i++) {
            if (i == 0)
                continue;
            if (i % (chunkSize + step) == 0) {
                ByteBuffer buf = ByteBuffer.wrap(fileBytes);

                /* DEBUGGING
                System.out.println("\nmod: " + i);

                System.out.println("BEFORE for i=" + i + ": " + Arrays.toString(fileBytes));
                */

                byte[] chunk = Arrays.copyOfRange(fileBytes, i-chunkSize,i);
                //System.out.println("chunk: " + Arrays.toString(chunk) + " (for i=" + i + ")");

                byte[] tempChunk = Arrays.copyOfRange(fileBytes, arrLen-i, arrLen-i+chunkSize);
                //System.out.println("tempChunk: " + Arrays.toString(tempChunk) + " (for i=" + i + ")");

                buf.position(i-chunkSize);
                buf.put(tempChunk);

                buf.position(arrLen-i);
                buf.put(chunk);


                fileBytes = buf.array();
                //System.out.println("AFTER for i=" + i + ": " + Arrays.toString(fileBytes));
            }
        }

        System.out.println("final: " + Arrays.toString(fileBytes));
        //System.out.println(fileBytes.length);

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
