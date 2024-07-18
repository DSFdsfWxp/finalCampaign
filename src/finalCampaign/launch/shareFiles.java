package finalCampaign.launch;

public abstract class shareFiles {
    public static String ExternalStoragePath;
    public static String LocalStoragePath;
    public static shareFiles instance;

    public abstract shareFi internalFile(String path);
    public abstract shareFi dataDirectory();
}