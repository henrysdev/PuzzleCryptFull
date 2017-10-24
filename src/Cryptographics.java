import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Cryptographics {

    // TODO reduce complexity. (currently naive approach with ~ O(2n) time)
    public byte[] scrambleBytes (byte[] fileBytes, int obfuscVal) throws Exception {

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
    }

    public byte[] hash (String message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashRes = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        return hashRes;
    }

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
