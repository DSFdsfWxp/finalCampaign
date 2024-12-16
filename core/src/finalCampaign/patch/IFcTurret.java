package finalCampaign.patch;

public interface IFcTurret {
    public byte[] fcSortfData();
    public void fcSortf(byte[] v);

    public boolean fcPreferBuildingTarget();
    public void fcPreferBuildingTarget(boolean v);
}
