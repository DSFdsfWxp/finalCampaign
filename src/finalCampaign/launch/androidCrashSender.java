package finalCampaign.launch;

import arc.util.*;

public class androidCrashSender extends shareCrashSender {
    public String createReport(String error) {
        String report =  "FinalCampaign Mod Android Launcher\n";
        report += "It has crashed.\n\n";

        report += "OS: " + OS.osName + " x" + (OS.osArchBits) + " (" + OS.osArch + ")\n";
        report += "Mod Android Launcher Version: " + bothLauncherVersion.toVersionString() + "\n";
        report += "Android API level: " + android.os.Build.VERSION.SDK_INT + "\n";
        report += "Java Version: " + OS.javaVersion + "\n";
        report += "Runtime Available Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "mb\n";
        report += "Cores: " + Runtime.getRuntime().availableProcessors() + "\n";
        report += "\n";

        return report + error;
    }
}
