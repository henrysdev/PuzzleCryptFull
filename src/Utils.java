import java.util.function.Consumer;
import java.util.function.Function;

public class Utils {

//TODO: Make sure Collector works with unboxed bytes[].
//    @SneakyThrows(IOException.class)
//    public static Collector<byte[], ?, ByteArrayOutputStream> byteCollector() {
//        return Collector.of(
//            () -> new ByteArrayOutputStream(),
//            (outputStream, inputObject) -> outputStream.write(inputObject),
//            (outputStream, otherStream) -> {outputStream.write(otherStream.toByteArray()); return outputStream;},
//            outputStream -> outputStream.toByteArray()
//        );
//    }

//TODO: Need this to handle IO exceptions in collector above.
//    public static RuntimeException wrap(Consumer<>) {
//        try {
//            return new String(bytes, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            throw Lombok.sneakyThrow(e);
//        }
//    }

    public static <F> Function<F, F> sideEffects(final Consumer<F> consumer) {
        return f -> {
            consumer.accept(f);
            return f;
        };
    }

}
