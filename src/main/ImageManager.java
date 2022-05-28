package main;

import recipes.Material;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ImageManager {
    private static final Map<Material, BufferedImage> materialImages = new HashMap<>();

    public static void loadMaterialImages() {
        for (Material m : Material.values()) {
            materialImages.put(m, Main.getImageFromResources("/images/items/" + String.valueOf(m).toLowerCase() + ".png"));
        }
    }

    public static BufferedImage getMaterialImage(Material m) {
        return materialImages.get(m);
    }
}
