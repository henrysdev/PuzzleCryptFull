import java.util.Stack;

public class DebugLogger {
    private static Stack<String> logs = new Stack<String>();

    public static void log(String entry)
    {
        logs.push(entry);
    }

    public static Stack<String> getLogs()
    {
        return logs;
    }
}
