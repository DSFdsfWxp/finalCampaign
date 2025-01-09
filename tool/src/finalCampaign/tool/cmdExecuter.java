package finalCampaign.tool;

import java.io.*;
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
        Process p = pb.start();
        InputStream pis = p.getInputStream();
        InputStream peis = p.getErrorStream();
        int code = p.waitFor();

        byte[] buff = new byte[pis.available()];
        pis.read(buff);
        System.out.write(buff);

        buff = new byte[peis.available()];
        peis.read(buff);
        System.out.write(buff);

        pis.close();
        peis.close();
        
        return code;
    }
}
