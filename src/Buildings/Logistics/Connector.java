package Buildings.Logistics;

import Buildings.Building;
import Buildings.WorldObject;
import Recipes.Material;
import Renderer.GraphicsPanel;
import Renderer.PointDouble;

import java.awt.*;

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
