package Renderer;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Satisgraphtory");
        ImageIcon icon = new ImageIcon("resources/images/satisgraphtory_icon.png");
        window.setIconImage(icon.getImage());

        GraphicsPanel gp = new GraphicsPanel();
        window.add(gp);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);
        gp.startThread();
    }
}
