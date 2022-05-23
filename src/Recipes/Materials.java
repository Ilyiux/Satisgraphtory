package Recipes;

import Buildings.BuildingType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Materials {
    public static Map<Material, Integer> sinkPoints = new HashMap<>();
    public static ArrayList<Material> invalidSinkItems = new ArrayList<>();

    public static void constructSink() {
        String text = "";
        try {
            text = Files.readString(Paths.get("resources/files/sinkpoints.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject all = new JSONObject(text);
        JSONArray allInvalid = all.getJSONArray("invalid");
        for (int i = 0; i < allInvalid.length(); i++) {
            invalidSinkItems.add(allInvalid.getEnum(Material.class, i));
        }
        JSONObject allValid = all.getJSONObject("valid");
        for (Iterator<String> it = allValid.keys(); it.hasNext(); ) {
            String s = it.next();
            sinkPoints.put(Material.valueOf(s), allValid.getInt(s));
        }
    }
}
