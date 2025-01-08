package finalCampaign.launch;

public abstract class files {
    public static String ExternalStoragePath;
    public static String LocalStoragePath;
    public static files instance;

    public abstract fi internalFile(String path);
    public abstract fi dataDirectory();
    public abstract fi rootDirectory();
}