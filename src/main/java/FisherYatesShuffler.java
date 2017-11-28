import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Collections;

public class FisherYatesShuffler {

    private static ArrayList<IntegerPair> swapsRecord=new ArrayList<>();
    private static long algoKey;

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
        for (int i = fileBytes.length - 1; i > 0; i--) {
            randInd = random.nextInt(i + 1);
            if (recording) {
                addSwapRecord(i, randInd);
            }
            byte temp = fileBytes[randInd];
            fileBytes[randInd] = fileBytes[i];
            fileBytes[i] = temp;
        }
        return fileBytes;
    }

    public static byte[] imitatedShuffle (byte[] fileBytes) {
        int randInd;

        for (int n = swapsRecord.size()-1; n > 0; n--) {
            IntegerPair pair = swapsRecord.get(n-1);
            randInd = pair.getB();
            int i   = pair.getA();

            byte temp = fileBytes[randInd];
            fileBytes[randInd] = fileBytes[i];
            fileBytes[i] = temp;
        }
        return fileBytes;
    }


    public static void addSwapRecord (int iInd, int rInd) {
        Integer i = new Integer(iInd);
        Integer randInd = new Integer(rInd);
        IntegerPair swap = new IntegerPair(i, randInd);
        swapsRecord.add(swap);
    }

    public static byte[] scramble (byte[] fileBytes) {
        return originalShuffle(fileBytes, false);
    }

    public static byte[] unscramble (byte[] fileBytes) {
        byte[] bullshitBytes = new byte[fileBytes.length];
        System.arraycopy(fileBytes, 0, bullshitBytes, 0, fileBytes.length);
        originalShuffle(bullshitBytes, true);
        byte[] retBytes = imitatedShuffle(fileBytes);
        return retBytes;//imitatedShuffle(fileBytes);
    }

}
