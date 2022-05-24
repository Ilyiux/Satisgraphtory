package recipes;

import buildings.BuildingType;

import java.util.Map;

public class Recipe {
    public String name;
    public BuildingType[] buildings;
    public Map<Material, Integer> input;
    public Map<Material, Integer> output;
    public double craftTime; //seconds

    public Recipe(String name, BuildingType[] buildings, Map<Material, Integer> input, Map<Material, Integer> output, double craftTime) {
        this.name = name;
        this.buildings = buildings;
        this.input = input;
        this.output = output;
        this.craftTime = craftTime;
    }
}
