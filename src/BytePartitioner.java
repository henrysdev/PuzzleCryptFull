import java.util.Arrays;
import java.util.Random;

public class BytePartitioner {
    public byte[][] splitWithRemainder (byte[] fileBytes, int n) throws Exception {
        int remainder = fileBytes.length % n;
        int fragSize = ((fileBytes.length - (remainder)) / n);

        // random index
        boolean remainderFlag = false;
        int ri = 0;
        if (remainder > 0) {
            remainderFlag = true;
            Random rand = new Random();
            ri = rand.nextInt(n - 0);
            //System.out.println(ri);
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
