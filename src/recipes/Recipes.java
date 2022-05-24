package recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import buildings.BuildingType;
import main.Main;
import org.json.*;

public class Recipes {
    public static ArrayList<Recipe> recipes = new ArrayList<>();

    public static void constructRecipes() {
        String text = Main.getFileFromResources("/files/recipes.json");
        JSONArray allRecipes = new JSONArray(text);
        for (int i = 0; i < allRecipes.length(); i++) {
            JSONObject o = (JSONObject) allRecipes.get(i);

            String name = o.getString("name");
            BuildingType[] buildings = new BuildingType[o.getJSONArray("produced_in").length()];
            for(int b = 0; b < o.getJSONArray("produced_in").length(); b++) {
                buildings[b] = o.getJSONArray("produced_in").getEnum(BuildingType.class, b);
            }
            double time = o.getDouble("time");

            Map<Material, Integer> input = new HashMap<>();
            JSONArray inputs = o.getJSONArray("ingredients");
            for (int ii = 0; ii < inputs.length(); ii++) {
                JSONObject m = inputs.getJSONObject(ii);
                input.put(m.getEnum(Material.class, "item"), m.getInt("amount"));
            }

            Map<Material, Integer> output = new HashMap<>();
            JSONArray outputs = o.getJSONArray("products");
            for (int oi = 0; oi < outputs.length(); oi++) {
                JSONObject m = outputs.getJSONObject(oi);
                output.put(m.getEnum(Material.class, "item"), m.getInt("amount"));
            }

            recipes.add(new Recipe(name, buildings, input, output, time));
        }
    }

    public static ArrayList<Recipe> getRecipeByBuilding(BuildingType building) {
        ArrayList<Recipe> outRecipes = new ArrayList<>();

        for (Recipe recipe : recipes) {
            if (Arrays.asList(recipe.buildings).contains(building)) outRecipes.add(recipe);
        }

        return outRecipes;
    }

    public static boolean isConveyorMaterial(Material m) {
        return m != Material.WATER && m != Material.CRUDE_OIL && m != Material.HEAVY_OIL_RESIDUE && m != Material.FUEL && m != Material.TURBOFUEL && m != Material.ALUMINA_SOLUTION && m != Material.SULFURIC_ACID && m != Material.NITROGEN_GAS && m != Material.NITRIC_ACID;
    }
}
