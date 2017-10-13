

public class FragmentationManager {

    private Shard[] shards;
    private String filepath;
    private int n;
    private String filePass;

    FragmentationManager (String[] args) {
        filepath = args[1];
        n = Integer.parseInt(args[2]);
        filePass = args[3];
        System.out.println("Fragmentation Successful");
    }

    public void scrambleBytes () {
        return;
    }

    public void partitionBytes () {
        return;
    }

    public void encryptShards () {
        return;
    }

    public void authenticateShards () {
        return;
    }

    public void getShards () {
        return;
    }
}
