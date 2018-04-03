import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FisherYatesShuffler {

    private static List<Integer> swapsRecord=new ArrayList<>();
    private static long algoKey;
    private static int step = 3;

    public FisherYatesShuffler (long key) {
        algoKey = key;
    }

    /** Fisher-Yates algorithm adapted from
     * https://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
     * @param fileBytes
     * @return fileBytes
     */
    public static byte[] originalShuffle (byte[] fileBytes, boolean recording) {
        int randInd;
        Random random = new Random(algoKey);
        for (int i = fileBytes.length - 1; i > 0; i-= step) {
            randInd = random.nextInt(i + 1);
            if (recording) {
                swapsRecord.add(randInd);
            }
            else {
                byte temp = fileBytes[randInd];
                fileBytes[randInd] = fileBytes[i];
                fileBytes[i] = temp;
            }
        }
        return fileBytes;
    }

    /** Use record of index swaps to unscramble the data using reverse
     * Fisher-Yates
     *
     * @param fileBytes
     * @return fileBytes
     */
    public static byte[] imitatedShuffle (byte[] fileBytes) {
        int randInd;
        int fbLength = fileBytes.length;
        for (int n = swapsRecord.size(); n > 0; n--) {
            randInd = swapsRecord.get(n-1);
            int i   = fbLength - (n * step) + (step-1);
            byte temp = fileBytes[randInd];
            fileBytes[randInd] = fileBytes[i];
            fileBytes[i] = temp;
        }
        return fileBytes;
    }

    /** Scramble target data via shuffling. Do not record moves.
     *
     * @param fileBytes
     * @return
     */
    public static byte[] scramble (byte[] fileBytes) {
        return originalShuffle(fileBytes, false);
    }

    /** Unscramble target data via observing the scramble algorithm,
     * recording the swaps made, the applying the inverse operations
     * on the target data.
     *
     * @param fileBytes
     * @return
     */
    public static byte[] unscramble (byte[] fileBytes) {
        swapsRecord.clear();
        byte[] bullshitBytes = new byte[fileBytes.length];
        System.arraycopy(fileBytes, 0, bullshitBytes, 0, fileBytes.length);
        originalShuffle(bullshitBytes, true);
        return imitatedShuffle(fileBytes);
    }

}
