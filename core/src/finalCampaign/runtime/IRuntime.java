package finalCampaign.runtime;

import arc.files.*;

public interface IRuntime {
    public String name();
    public String getVersion();
    public Fi getRootPath();
    public Fi getGameJar();
    public void install(Fi mod) throws Exception;
    public void startupInstall() throws Exception;
}
