package main;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ColorManager {
    private static Map<String, Color> colors;

    public static void initializeColors() {
        colors = new HashMap<>();
        String data = Main.getFileFromResources("/files/settings.txt");
        ArrayList<String> args = new ArrayList<>();
        for (String l : data.split("[{}]")) {
            if (l.length() == 0 || l.startsWith("#")) continue;
            args.add(l);
        }
        for (String arg : args) {
            String[] a = arg.split("=");
            Color c = switch (a[1]) {
                case "RED":
                    yield new Color(255, 0, 0);
                case "ORANGE":
                    yield new Color(255, 127, 0);
                case "YELLOW":
                    yield new Color(255, 255, 0);
                case "GREEN":
                    yield new Color(0, 255, 0);
                case "BLUE":
                    yield new Color(0, 0, 255);
                case "PURPLE":
                    yield new Color(255, 0, 255);
                default:
                    throw new IllegalStateException("Unexpected value: " + a[1]);
            };

            colors.put(a[0], c);
        }
    }

    public static Color getColor(String arg) {
        return colors.getOrDefault(arg, Color.BLACK);
    }
}
