import lombok.Lombok;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Utils {

//TODO: Make sure Collector works with unboxed bytes[].
    @SneakyThrows
    public static Collector<byte[], ByteArrayOutputStream, byte[]> byteCollector() {
        return Collector.of(
            wrapSupplier(() -> new ByteArrayOutputStream()),
            wrapBiConsumer((outputStream, inputObject) -> outputStream.write(inputObject)),
            (outputStream, otherStream) -> {outputStream.write(otherStream.toByteArray()); return outputStream;},
            outputStream -> outputStream.toByteArray()
        );
    }

    public static <T> WrappedSupplier<T> wrapSupplier(final WrappedSupplier<T> supplier) {
        return supplier;
    }

    public static <T, U> WrappedBiConsumer<T, U> wrapBiConsumer(final WrappedBiConsumer<T, U> consumer) {return consumer;}

    @FunctionalInterface
    public interface WrappedBiConsumer<T, U> extends BiConsumer<T, U> {
        @Override
        default void accept(T input, U input2) {
            try {
                acceptImpl(input, input2);
            } catch (final Throwable t) {
                throw Lombok.sneakyThrow(t);
            }
        }

        /**
         * @param input the input to process.
         * @throws Throwable if there is any exception.
         */
        void acceptImpl(T input, U input2) throws Throwable;
    }

    @FunctionalInterface
    public interface WrappedSupplier<T> extends Supplier<T>, Callable<T> {
        @Override
        default T get() {
            try {
                return getImpl();
            } catch (final Throwable t) {
                throw Lombok.sneakyThrow(t);
            }
        }

        @Override
        default T call() {
            return get();
        }

        /**
         * @return a result of type T.
         * @throws Throwable if there are any exceptions.
         */
        T getImpl() throws Throwable;
    }

    public static <F> Function<F, F> sideEffects(final Consumer<F> consumer) {
        return f -> {
            consumer.accept(f);
            return f;
        };
    }

}
