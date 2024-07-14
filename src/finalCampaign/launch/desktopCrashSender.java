package finalCampaign.launch;

import arc.util.*;

public class desktopCrashSender extends shareCrashSender {
    public String createReport(String error) {
        String report =  "FinalCampaign Mod Desktop Launcher\n";
        report += "It has crashed.\n\n";

        report += "OS: " + OS.osName + " x" + (OS.osArchBits) + " (" + OS.osArch + ")\n";
        report += "Mod Desktop Launcher Version: " + bothLauncherVersion.toDesktopVersionString() + "\n";
        report += "Java Version: " + OS.javaVersion + "\n";
        report += "Runtime Available Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "mb\n";
        report += "Cores: " + Runtime.getRuntime().availableProcessors() + "\n";
        report += "\n";

        return report + error;
    }
}
