import java.util.Arrays;

public class IntegerPair {
    private final Integer a;
    private final Integer b;

    public IntegerPair(Integer first, Integer second)
    {
        a = first;
        b = second;
    }

    public Integer getA()   { return a; }
    public Integer getB() { return b; }

    @Override
    public String toString () {
        return "(" + getA().toString() + ", " + getB().toString() + ")";
    }
}
