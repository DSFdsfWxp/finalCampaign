package finalCampaign.map;

import java.io.*;
import mindustry.game.*;
import mindustry.io.SaveFileReader.*;

public class initialMode implements CustomChunk {
    @Override
    public void write(DataOutput stream) throws IOException {
        stream.writeInt(fcMap.initialMode == null ? -1 : fcMap.initialMode.ordinal());
    }

    @Override
    public void read(DataInput stream) throws IOException {
        int ordinal = stream.readInt();
        fcMap.initialMode = ordinal == -1 ? null : Gamemode.all[ordinal];
    }

    @Override
    public boolean shouldWrite() {
        return !fcMap.exportingPlainSave;
    }
}
