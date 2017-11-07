import lombok.SneakyThrows;
import lombok.val;

import java.util.Arrays;
import java.util.zip.*;

import java.io.ByteArrayOutputStream;

public class PuzzleFile {

    byte[] fileBytes;
    String secretKey;

    public PuzzleFile (byte[] fb, String secretKey) {
        this.fileBytes = fb;
        this.secretKey = secretKey;
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
        fileBytes = Cryptographics.scrambleBytes(fileBytes);
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
    public Shard[] toShards (int n) {
        fileBytes = Cryptographics.scrambleBytes(fileBytes);
        byte[][] payloads = BytePartitioner.splitWithRemainder(fileBytes, n);

        val aesCipher = new AESEncrypter(secretKey, new byte[0]);
        byte[] IV = aesCipher.getInitV();

        // process each payload into a complete fragment, iterating by sequenceID
        Shard[] shards = new Shard[n];
        for (int seqID = 0; seqID < n; seqID++) {
            System.out.println(seqID);
            // encrypt payloads
            byte[] encrPayload = aesCipher.encrypt(payloads[seqID]);

            // generate and append HMAC
            byte[] hmac = Cryptographics.hash(secretKey.concat(Integer.toString(seqID)));

            // store as shard
            Shard shard = new Shard(encrPayload, IV, hmac);
            shards[seqID] = shard;
        }

        return shards;
    }
}
