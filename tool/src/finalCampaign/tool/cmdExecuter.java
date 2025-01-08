package finalCampaign.tool;

import arc.struct.*;

public class cmdExecuter {
    private String command;

    public cmdExecuter(String cmd) {
        command = cmd;
    }

    public int exec(String args[]) throws Exception {
        Seq<String> cmds = new Seq<>();
        cmds.add(command);
        cmds.add(args);
        ProcessBuilder pb = new ProcessBuilder(cmds.toArray(String.class));
        return pb.start().waitFor();
    }
}
