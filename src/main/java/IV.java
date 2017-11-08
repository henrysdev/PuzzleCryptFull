import java.util.Arrays;

public class IV {
    private byte[] value;

    public IV (byte[] v) {
        this.value = v;
    }

    public byte[] getValue () {
        return value;
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }
}
