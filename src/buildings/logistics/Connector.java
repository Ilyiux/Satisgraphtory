package buildings.logistics;

import buildings.Building;
import buildings.WorldObject;
import recipes.Material;
import main.PointDouble;

public class Connector extends WorldObject {
    public int tier;

    public boolean editingRateCap = false;
    public String rateCapString;
    public double rateCap = -1;

    public PointDouble lineCenter;

    public double rate;
    public double outRate;
    public double maxRate;
    public double outMaxRate;

    public Material type = null;

    public boolean invalidState = false;
    public boolean inefficientState = false;

    public Building startBuilding;
    public Building endBuilding;
}
