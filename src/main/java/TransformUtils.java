import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Random;

public class TransformUtils {
    /** Given a chunk of data and an integer n, divide this chunk
     * of data into n equal sized parts, with the remainder tacked on
     * to a random one of these parts.
     *
     * @param fileBytes
     * @param n
     * @return payloads
     */
    @SneakyThrows
    public static byte[][] splitWithRemainder (byte[] fileBytes, int n) {
        int remainder = fileBytes.length % n;
        int fragSize = ((fileBytes.length - (remainder)) / n);

        // determine random index
        boolean remainderFlag = false;
        int ri = 0;
        if (remainder > 0) {
            remainderFlag = true;
            Random rand = new Random();
            ri = rand.nextInt(n - 0);
        }

        // handle offset for largest byte
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
}
