package Renderer;

import Recipes.Material;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ImageManager {
    private static final Map<Material, BufferedImage> materialImages = new HashMap<>();

    public static void loadMaterialImages() {
        try {
            for (Material m : Material.values()) {
                materialImages.put(m, ImageIO.read(new File("resources/images/items/" + String.valueOf(m).toLowerCase() + ".png")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage getImageFromResources(String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(Objects.requireNonNull(Main.class.getResource(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static String getFileFromResources(String path) {
        String text = "";
        try {
            InputStream is = Main.class.getResourceAsStream(path);
            assert is != null;
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            br.close();
            isr.close();
            is.close();
            text =  sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static BufferedImage getMaterialImage(Material m) {
        return materialImages.get(m);
    }
}
