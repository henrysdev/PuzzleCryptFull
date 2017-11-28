import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

public class CryptoUtils {

    /** Scrambles bytes in a reversible manner TODO make stronger
     *
     * @param fileBytes
     * @return
     * @throws Exception
     */
    @SneakyThrows
    public static byte[] scrambleBytes (byte[] fileBytes) {
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

        return fileBytes;
    }

    /** bit shift logic borrowed from
    * https://stackoverflow.com/questions/19181411/circular-rotate-issue-with-rotate-left
     */
    public static byte rotateRight(byte bits, int shift)
    {
        return (byte)(((bits & 0xff)  >>> shift) | ((bits & 0xff) << (8 - shift)));
    }

    public static byte rotateLeft(byte bits, int shift)
    {
        return (byte)(((bits & 0xff) << shift) | ((bits & 0xff) >>> (8 - shift)));
    }

    /** Generate the SHA-256 hash for a given string
     *
     * @param message
     * @return hashResult
     */
    @SneakyThrows
    public static byte[] hash (String message) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashResult = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        return hashResult;
    }

    /** Create a random string of data of a given length and return
     * the byte array representation
     *
     * @param blockLen
     * @return block
     */
    public static byte[] randomBlock (int blockLen) {

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
