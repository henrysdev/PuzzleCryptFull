public class Cryptographics {

    // TODO reduce complexity. (currently naive approach with ~ O(2n) time)
    public byte[] scrambleBytes (byte[] fileBytes, int obfuscVal) {

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
}
