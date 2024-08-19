package finalCampaign.launch;

public abstract class bothFiles {
    public static String ExternalStoragePath;
    public static String LocalStoragePath;
    public static bothFiles instance;

    public abstract bothFi internalFile(String path);
    public abstract bothFi dataDirectory();
}