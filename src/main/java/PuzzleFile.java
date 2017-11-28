import lombok.SneakyThrows;
import lombok.val;

import java.util.Arrays;
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

    public PuzzleFile (byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    public byte[] toByteArray () {
        return fileBytes;
    }

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

    @SneakyThrows
    public void addChunk (byte[] chunk) {
        // append file info to file data to form complete file data
        ByteArrayOutputStream compFileDataStream = new ByteArrayOutputStream();
        compFileDataStream.write(fileBytes);
        compFileDataStream.write(chunk);
        fileBytes = compFileDataStream.toByteArray();
    }

    @SneakyThrows
    public void scramble () {
        fileBytes = CryptoUtils.scrambleBytes(fileBytes);
    }

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

    @SneakyThrows
    public Payload[] splitIntoPayloads (int n) {
        byte[][] bytePayloads = TransformUtils.splitWithRemainder(fileBytes, n);
        Payload[] payloads = new Payload[n];
        for (int seqID = 0; seqID < n; seqID++) {
            payloads[seqID] = new Payload(bytePayloads[seqID]);
        }
        return payloads;
    }
}
