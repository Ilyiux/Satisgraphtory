package buildings.logistics;

import buildings.Building;
import buildings.WorldObject;
import recipes.Material;
import main.PointDouble;

public class Connector extends WorldObject {
    public int maxTier;
    public int tier;

    public PointDouble lineCenter;

    public double rate;
    public int maxRate;

    public Material type;

    public boolean invalidState = false;

    public Building startBuilding;
    public Building endBuilding;
}
