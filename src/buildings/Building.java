package buildings;

import buildings.logistics.Conveyor;
import buildings.logistics.Pipe;
import recipes.Material;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Building extends WorldObject {
    public Point position;

    public Map<Material, Double> inItems = new HashMap<>();

    public int maxInConveyors;
    public ArrayList<Conveyor> inConveyors = new ArrayList<>();
    public int maxOutConveyors;
    public ArrayList<Conveyor> outConveyors = new ArrayList<>();
    public ArrayList<Double> outConveyorRate = new ArrayList<>();
    public ArrayList<Material> outConveyorType = new ArrayList<>();
    public int maxInPipes;
    public ArrayList<Pipe> inPipes = new ArrayList<>();
    public int maxOutPipes;
    public ArrayList<Pipe> outPipes = new ArrayList<>();
    public ArrayList<Double> outPipeRate = new ArrayList<>();
    public ArrayList<Material> outPipeType = new ArrayList<>();

    public double power;
}
