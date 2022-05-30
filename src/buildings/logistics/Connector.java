package buildings.logistics;

import buildings.Building;
import buildings.WorldObject;
import recipes.Material;
import main.PointDouble;

public class Connector extends WorldObject {
    public int tier;

    public PointDouble lineCenter;

    public double rate;
    public double outRate;
    public int maxRate;

    public Material type;

    public boolean invalidState = false;
    public boolean inefficientState = false;

    public Building startBuilding;
    public Building endBuilding;
}
