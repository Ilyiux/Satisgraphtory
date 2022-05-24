package recipes;

import main.Main;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Materials {
    public static Map<Material, Integer> sinkPoints = new HashMap<>();
    public static ArrayList<Material> invalidSinkItems = new ArrayList<>();

    public static void constructSink() {
        String text = Main.getFileFromResources("/files/sinkpoints.json");
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
