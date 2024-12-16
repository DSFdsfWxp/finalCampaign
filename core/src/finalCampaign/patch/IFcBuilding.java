package finalCampaign.patch;

public interface IFcBuilding {
    public boolean fcSetModeSelected();
    public void fcSetModeSelected(boolean v);

    public boolean fcForceDisable();
    public void fcForceDisable(boolean v);

    public boolean fcForceEnable();
    public void fcForceEnable(boolean v);

    public String fcStatus();
    public void fcStatus(String v);

    public boolean fcInfinityPower();
    public void fcInfinityPower(boolean v);
}
