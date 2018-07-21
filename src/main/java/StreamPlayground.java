import java.util.*;
import java.util.stream.Stream;
public class StreamPlayground {

    public static void main (String[] args) {
        List<String> wordList = new ArrayList<String>();
        wordList.add("Henry");
        wordList.add("Edward");
        wordList.add("Warren");

        Stream<String> words = wordList.stream();
        Stream<String> longWords = words.filter(w -> w.length() > 12);


    }
}
