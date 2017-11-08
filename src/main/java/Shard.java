import lombok.Data;

import java.util.Arrays;
import java.util.stream.Stream;

@Data
public class Shard {

    private final Payload payload;
    private final IV iv;
    private final HMAC hmac;

    public byte[] toFragment () {
        return Stream.of(payload.getValue(), iv.getValue(), hmac.getValue())
            .collect(Utils.byteCollector());
    }

    @Override
    public String toString() {
        return String.format (
                "PAYLOAD: " + payload +
                "\nIV: " + iv +
                "\nHMAC: " + hmac +
                "\n"
        );
    }

}