import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class PuzzleFile {

    private byte[] fileBytes;
    private String secretKey;

    public PuzzleFile (byte[] fileBytes, String secretKey) {
        this.fileBytes = fileBytes;
        this.secretKey = secretKey;
    }

    /** Alternate constructor for creating an instance of a
     * pre-generated PuzzleFile object in byte array representation
     *
     * @param fileBytes
     */
    public PuzzleFile (byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    /** Return data in a byte array representation
     *
     * @return fileBytes
     */
    public byte[] toByteArray () {
        return this.fileBytes;
    }

    /** Given two bounding indices (inclusive), copy the chunk
     * of data between these bounds if possible and return as a
     * chunk (byte array representation)
     *
     * @param startPos
     * @param endPos
     * @return chunk
     */
    @SneakyThrows
    public byte[] getChunk (int startPos, int endPos) {
        byte[] chunk = new byte[ endPos - startPos + 1 ];
        try {
            chunk = Arrays.copyOfRange(fileBytes, startPos, endPos);
        } catch (IndexOutOfBoundsException ioobe) {
            System.out.println("invalid indice range for getChunk");
        }
        return chunk;
    }

    public int getSize () {
        return fileBytes.length;
    }

    /** Append a new chunk of data to the object via a byte stream.
     *
     * @param chunk
     */
    @SneakyThrows
    public void addChunk (byte[] chunk) {
        /* append file info to file data to form complete file data
         */
        ByteArrayOutputStream compFileDataStream = new ByteArrayOutputStream();
        compFileDataStream.write(fileBytes);
        compFileDataStream.write(chunk);
        fileBytes = compFileDataStream.toByteArray();
    }

    /** Scramble the data using the reversible scramble algorithm.
     * Break data into 100kb blocks and scramble each block before
     * reappending them back together.
     *
     */
    @SneakyThrows
    public void scramble () {
        long algoKey = CryptoUtils.generateLong(secretKey);
        FisherYatesShuffler shuffler = new FisherYatesShuffler(algoKey);

        byte[][] blocks = obtainScrambleBlocks();

        // scramble and reappend each block
        ByteArrayOutputStream scramStream = new ByteArrayOutputStream();
        fileBytes = new byte[0]; // temp mem free
        for (int i = 0; i < blocks.length; i++) {
            byte[] currLoad = shuffler.scramble(blocks[i]);
            scramStream.write(currLoad);
            blocks[i] = new byte[0]; // mem free
        }

        this.fileBytes = scramStream.toByteArray();
    }

    /** Unscramble the data using the reversible scramble algorithm.
     * Break file bytes back into blocks and unscramble each block before
     * reappending them all back together.
     */
    @SneakyThrows
    public void unscramble () {
        long algoKey = CryptoUtils.generateLong(secretKey);
        FisherYatesShuffler shuffler = new FisherYatesShuffler(algoKey);

        byte[][] blocks = obtainScrambleBlocks();

        // unscramble and reappend blocks
        ByteArrayOutputStream unscramStream = new ByteArrayOutputStream();
        fileBytes = new byte[0]; // temp mem free
        for (int i = 0; i < blocks.length; i++) {
            byte[] currLoad = shuffler.unscramble(blocks[i]);
            unscramStream.write(currLoad);
            blocks[i] = new byte[0]; // temp mem free
        }

        this.fileBytes = unscramStream.toByteArray();
    }

    /** Compress the data using Gzip
     *
     */
    @SneakyThrows
    public void compress () {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream(fileBytes.length);
        try {
            GZIPOutputStream gzipStream = new GZIPOutputStream(bStream);
            try {
                gzipStream.write(fileBytes);
            }
            finally {
                gzipStream.close();
            }
        }
        finally {
            bStream.close();
        }
        fileBytes = bStream.toByteArray();
    }

    /** Decompress the data using Gzip
     *
     */
    @SneakyThrows
    public void decompress () {
        ByteArrayInputStream bStream = new ByteArrayInputStream(fileBytes);
        GZIPInputStream gzipStream = new GZIPInputStream(bStream);
        BufferedReader br = new BufferedReader(new InputStreamReader(gzipStream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gzipStream.close();
        bStream.close();
        fileBytes = sb.toString().getBytes();
    }

    /** Given a desired number of fragments to split into, create
     * this number of payloads and return when completed as an
     * array of Payloads.
     *
     * @param n
     * @return payloads
     */
    @SneakyThrows
    public Payload[] splitIntoPayloads (int n) {
        byte[][] bytePayloads = CryptoUtils.splitWithRemainder(fileBytes, n, false);
        Payload[] payloads = new Payload[n];
        for (int seqID = 0; seqID < n; seqID++) {
            payloads[seqID] = new Payload(bytePayloads[seqID]);
        }
        return payloads;
    }

    public byte[][] obtainScrambleBlocks () {
        // determine block count
        int blockCount = fileBytes.length / 100000;
        if (blockCount == 0)
            blockCount = 1;

        // split file data back into blocks
        byte[][] blocks = CryptoUtils.splitWithRemainder(fileBytes, blockCount, true);
        return blocks;
    }
}
