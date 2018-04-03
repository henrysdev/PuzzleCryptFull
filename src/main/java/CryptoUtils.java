import lombok.SneakyThrows;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

public class CryptoUtils {

    /** bit shift logic borrowed from
    * https://stackoverflow.com/questions/19181411/circular-rotate-issue-with-rotate-left
     */
    public static byte rotateRight(byte bits, int shift) {
        return (byte)(((bits & 0xff)  >>> shift) | ((bits & 0xff) << (8 - shift)));
    }

    public static byte rotateLeft(byte bits, int shift) {
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

    /** Given a chunk of data and an integer n, divide this chunk
     * of data into n equal sized parts, with the remainder tacked on
     * to a random one of these parts.
     *
     * @param fileBytes
     * @param n
     * @return payloads
     */
    @SneakyThrows
    public static byte[][] splitWithRemainder (byte[] fileBytes, int n, boolean endRem) {
        int remainder = fileBytes.length % n;
        int fragSize = ((fileBytes.length - (remainder)) / n);

        /* determine random index
         */
        boolean remainderFlag = false;
        int ri = 0;
        if (remainder > 0) {
            remainderFlag = true;
            Random rand = new Random();
            ri = rand.nextInt(n - 0);
        }

        // end remainder condition
        if (endRem) {
            ri = n - 1;
        }

        /* handle offset for largest byte
         */
        byte[][] payloads = new byte[n][];
        int offsetBeg = 0;
        int offsetEnd = fragSize;
        for (int i = 0; i < n; i++) {
            if (remainderFlag) {
                if (i == ri) {
                    offsetEnd += remainder;
                }
            }
            byte[] newPayload = Arrays.copyOfRange(fileBytes, offsetBeg, offsetEnd);
            payloads[i] = newPayload;
            offsetBeg = offsetEnd;
            offsetEnd += fragSize;
        }
        return payloads;
    }

    /** Return a casted-to-long numerical representation
     * of a String.
     *
     * @param str
     * @return
     */
    public static long generateLong (String str) {
        return new Long(str.hashCode());
    }
}
