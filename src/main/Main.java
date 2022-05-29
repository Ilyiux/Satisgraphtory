package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setMinimumSize(new Dimension(GraphicsPanel.MIN_WIDTH, GraphicsPanel.MIN_HEIGHT));
        window.setResizable(true);
        window.setTitle("Satisgraphtory");
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/images/satisgraphtory_icon.png")));
        window.setIconImage(icon.getImage());

        GraphicsPanel gp = new GraphicsPanel();
        window.add(gp);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);
        gp.startThread();
    }


    public static BufferedImage getImageFromResources(String path) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream(path)));
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
            text = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }
}
