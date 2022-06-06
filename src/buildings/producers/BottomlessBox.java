package buildings.producers;

import buildings.Building;
import main.*;
import recipes.Recipes;
import recipes.Material;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class BottomlessBox extends Building {
    public boolean materialSet = false;
    public Material material;
    private final ArrayList<Material> possibleMaterials = new ArrayList<>();
    private int materialScroll = 0;

    private boolean editingRate;
    private String rateString;
    public double rate;

    private BufferedImage image;

    public BottomlessBox(Point position) {
        this.position = position;
        maxInConveyors = 0;
        maxOutConveyors = 1;
        maxInPipes = 0;
        maxOutPipes = 1;
        getImages();
        initializeRecipes();
    }

    public BottomlessBox(Point position, boolean materialSet, Material material, double rate) {
        this.position = position;
        this.materialSet = materialSet;
        this.material = material;
        this.rate = rate;
        maxInConveyors = 0;
        maxOutConveyors = 1;
        maxInPipes = 0;
        maxOutPipes = 1;
        getImages();
        initializeRecipes();
    }

    private void initializeRecipes() {
        possibleMaterials.addAll(Arrays.asList(Material.values()));
    }

    private void getImages() {
        image = Main.getImageFromResources("/images/buildings/generator.png");
    }

    public void update() {
        if (materialSet && !editingRate) updateShownRates();

        updateOutItems();
    }

    private void exitRateInput() {
        editingRate = false;

        if (rateString.length() == 0) rateString = "0";
        rate = Double.parseDouble(rateString);
        if (rate < 0) rate = 0;

        rate = (double)Math.round(rate * 10000) / 10000;

        rateString = "" + rate;
    }

    private void updateShownRates() {
        rateString = String.valueOf(rate);
    }

    private void updateOutItems() {
        outConveyorRate.clear();
        outConveyorType.clear();
        outPipeRate.clear();
        outPipeType.clear();
        if (materialSet) {
            if (Recipes.isConveyorMaterial(material)) {
                outConveyorRate.add(rate);
                outConveyorType.add(material);
            } else {
                outPipeRate.add(rate);
                outPipeType.add(material);
            }
        }
        for (int c = outConveyorRate.size(); c < maxOutConveyors; c++)
            outConveyorRate.add(-1.0);
        for (int p = outPipeRate.size(); p < maxOutPipes; p++)
            outPipeRate.add(-1.0);
    }

    public void clicked(GraphicsPanel gp, int mx, int my) {
        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5), gp);
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 150);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 150);
        if (menuTopLeft.y < 0) {
            menuBottomRight.y -= menuTopLeft.y;
            menuTopLeft.y -= menuTopLeft.y;
        }
        if (menuBottomRight.y > gp.getHeight()) {
            menuTopLeft.y -= menuBottomRight.y - gp.getHeight();
            menuBottomRight.y -= menuBottomRight.y - gp.getHeight();
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(1);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(1);
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6) {
            menuTopLeft.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6);
            menuBottomRight.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6);
        }

        int buttonHeight = (int)((gp.getHeight() * 1.77777777) / 6 * 0.08);
        int spacing = buttonHeight / 4;
        int buttonWidth = (int) ((gp.getHeight() * 1.77777777) / 6 - spacing * 2);

        if (editingRate) {
            exitRateInput();
        } else {
            if ((mx < menuTopLeft.x || mx > menuBottomRight.x || my < menuTopLeft.y || my > menuBottomRight.y) && mx < gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6)
                gp.closeBuildingMenu();
        }

        if (mx > menuTopLeft.x + 105 && mx < menuTopLeft.x + 190 && my > menuTopLeft.y + 260 && my < menuTopLeft.y + 290) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 95 && my > menuTopLeft.y + 260 && my < menuTopLeft.y + 290) {
            gp.closeBuildingMenu();
            gp.addBuilding(new BottomlessBox(position, materialSet, material, rate));
        }

        if (mx > gp.getWidth() - buttonWidth - spacing && mx < gp.getWidth() - spacing) {
            ArrayList<Material> showMaterials = new ArrayList<>(possibleMaterials.subList(materialScroll, Math.min(possibleMaterials.size(), materialScroll + (gp.getHeight() - spacing) / (spacing + buttonHeight))));
            for (int i = 0; i < showMaterials.size(); i++) {
                if (my > spacing + i * (spacing + buttonHeight) && my < spacing + buttonHeight + i * (spacing + buttonHeight)) {
                    materialSet = true;
                    material = showMaterials.get(i);
                }
            }
        }

        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 120 && my > menuTopLeft.y + 135 && my < menuTopLeft.y + 155) {
            editingRate = true;
        }
    }

    public void typed(GraphicsPanel gp, int keyCode) {
        if (keyCode == KeyEvent.VK_X) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (keyCode == KeyEvent.VK_D) {
            gp.closeBuildingMenu();
            gp.addBuilding(new BottomlessBox(position, materialSet, material, rate));
        }
        if (editingRate) {
            if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                if (rateString.length() > 0)
                    rateString = rateString.substring(0, rateString.length() - 1);
            } else if (keyCode == KeyEvent.VK_PERIOD) {
                rateString = rateString + ".";
            } else if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9){
                rateString = rateString + (keyCode - 48);
            }
            if (keyCode == KeyEvent.VK_ENTER) exitRateInput();
        }
    }

    public void scrolled(GraphicsPanel gp, int scrollAmount) {
        int buttonHeight = (int)((gp.getHeight() * 1.77777777) / 6 * 0.08);
        int spacing = buttonHeight / 4;

        if (possibleMaterials.size() >= materialScroll + (gp.getHeight() - spacing) / (buttonHeight + spacing)) {
            materialScroll += scrollAmount;
            if (materialScroll < 0) materialScroll = 0;
            if (materialScroll + (gp.getHeight() - spacing) / (spacing + buttonHeight) > possibleMaterials.size()) materialScroll = possibleMaterials.size() - (gp.getHeight() - spacing) / (spacing + buttonHeight);
        }
    }

    public void draw(boolean greyedOut, Graphics2D g2d, GraphicsPanel gp) {
        Point start = Screen.convertToScreenPoint(new PointDouble(position.x, position.y), gp);
        Point end = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 1), gp);

        g2d.drawImage(image, start.x, start.y, end.x - start.x, end.y - start.y, null);

        if (!materialSet) {
            g2d.setColor(ColorManager.getColor("invalid"));
        } else {
            g2d.setColor(ColorManager.getColor("valid"));
        }
        if (greyedOut) g2d.setColor(Color.GRAY);
        g2d.drawRoundRect(start.x, start.y, end.x - start.x, end.y - start.y, (int) (Screen.getZoom() / 10), (int) (Screen.getZoom() / 10));

        double size = Screen.convertLengthToScreenLength(0.08);
        for (int ic = 0; ic < maxInConveyors; ic++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x, position.y + ((double)(ic + 1) / (maxInConveyors + maxInPipes + 1))), gp);
            g2d.setColor(Color.GRAY);
            g2d.fillRect((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }
        for (int ip = 0; ip < maxInPipes; ip++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x, position.y + ((double)(ip + maxInConveyors + 1) / (maxInConveyors + maxInPipes + 1))), gp);
            g2d.setColor(Color.ORANGE);
            g2d.fillOval((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }
        for (int oc = 0; oc < maxOutConveyors; oc++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + ((double)(oc + 1) / (maxOutConveyors + maxOutPipes + 1))), gp);
            g2d.setColor(Color.GRAY);
            g2d.fillRect((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }
        for (int op = 0; op < maxOutPipes; op++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + ((double)(op + maxOutConveyors + 1) / (maxOutConveyors + maxOutPipes + 1))), gp);
            g2d.setColor(Color.ORANGE);
            g2d.fillOval((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, Screen.convertLengthToScreenLength(0.15)));

        Point namePos = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.15), gp);
        String nameString = "Box";
        g2d.drawString(nameString, namePos.x, namePos.y);

        Point ratePos = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.95), gp);
        if (materialSet) {
            g2d.drawString(rate + "/min", ratePos.x, ratePos.y);
        } else {
            g2d.drawString("-/min", ratePos.x, ratePos.y);
        }

        if (materialSet) {
            Point imageStart = Screen.convertToScreenPoint(new PointDouble(position.x + 0.72, position.y + 0.72), gp);
            Point imageEnd = Screen.convertToScreenPoint(new PointDouble(position.x + 0.97, position.y + 0.97), gp);
            g2d.drawImage(ImageManager.getMaterialImage(material), imageStart.x, imageStart.y, imageEnd.x - imageStart.x, imageEnd.y - imageStart.y, null);
        }

    }

    public void drawMenu(Graphics2D g2d, GraphicsPanel gp) {
        g2d.setColor(new Color(0, 0, 0, 63));
        g2d.fillRect(0, 0, gp.getWidth(), gp.getHeight());

        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5), gp);
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 150);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 150);
        if (menuTopLeft.y < 0) {
            menuBottomRight.y -= menuTopLeft.y;
            menuTopLeft.y -= menuTopLeft.y;
        }
        if (menuBottomRight.y > gp.getHeight()) {
            menuTopLeft.y -= menuBottomRight.y - gp.getHeight();
            menuBottomRight.y -= menuBottomRight.y - gp.getHeight();
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(1);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(1);
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6) {
            menuTopLeft.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6);
            menuBottomRight.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6);
        }

        g2d.setColor(new Color(30, 32, 30));
        g2d.fillRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);
        g2d.setColor(new Color(90, 95, 90));
        g2d.drawRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 20));
        g2d.drawString("Bottomless Box", menuTopLeft.x + 10, menuTopLeft.y + 25);
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 35, menuTopLeft.x + 190, menuTopLeft.y + 35);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Item", menuTopLeft.x + 10, menuTopLeft.y + 70);
        g2d.drawString("Rate", menuTopLeft.x + 10, menuTopLeft.y + 130);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(new Color(232, 79, 79));
        g2d.fillRoundRect(menuTopLeft.x + 105, menuTopLeft.y + 260, 85, 30, 5, 5);
        g2d.setColor(new Color(79, 79, 232));
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 260, 85, 30, 5, 5);
        g2d.setColor(new Color(30, 32, 30));
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Delete", menuTopLeft.x + 123, menuTopLeft.y + 280);
        g2d.drawString("Duplicate", menuTopLeft.x + 18, menuTopLeft.y + 280);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 12));
        g2d.setColor(new Color(90, 95, 90));

        Color displayBackgroundColor = new Color(20, 22, 20);

        // material
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 75, 180, 20, 8, 8);
        if (materialSet) {
            g2d.setColor(Color.WHITE);
            g2d.drawString(String.valueOf(material).replace("_", " "), menuTopLeft.x + 15, menuTopLeft.y + 89);
        }

        int buttonHeight = (int)((gp.getHeight() * 1.77777777) / 6 * 0.08);
        int spacing = buttonHeight / 4;
        int buttonWidth = (int) ((gp.getHeight() * 1.77777777) / 6 - spacing * 2);
        int textSize = buttonHeight / 2;

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, textSize));
        g2d.setColor(new Color(34, 36, 34));
        g2d.fillRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6, 0, (int)(gp.getHeight() * 1.77777777) / 6, gp.getHeight());
        ArrayList<Material> showMaterials = new ArrayList<>(possibleMaterials.subList(materialScroll, Math.min(possibleMaterials.size(), materialScroll + (gp.getHeight() - spacing) / (buttonHeight + spacing))));
        for (Material material : showMaterials) {
            int index = showMaterials.indexOf(material);

            g2d.setColor(displayBackgroundColor);
            g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing, spacing + index * (spacing + buttonHeight), buttonWidth, buttonHeight, 10, 10);
            if (materialSet && material == this.material) {
                g2d.setColor(new Color(100, 104, 100));
                g2d.drawRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing, spacing + index * (spacing + buttonHeight), buttonWidth, buttonHeight, 10, 10);
            }
            g2d.setColor(Color.WHITE);
            g2d.drawString(String.valueOf(material).replace("_", " "), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + (buttonHeight - 4) + spacing * 3, spacing * 4 + index * (spacing + buttonHeight));
            g2d.drawImage(ImageManager.getMaterialImage(material), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing * 2, spacing + 2 + index * (spacing + buttonHeight), buttonHeight - 4, buttonHeight - 4, null);
        }

        if (possibleMaterials.size() > (gp.getHeight() - 10) / 40) {
            int rHeight = (gp.getHeight() - spacing * 2) / possibleMaterials.size();
            g2d.setColor(new Color(25, 27, 25));
            g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing / 4, spacing, spacing / 2, possibleMaterials.size() * rHeight, 5, 5);
            g2d.setColor(new Color(60, 64, 60));
            g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing / 4, spacing + rHeight * materialScroll, spacing / 2, rHeight * ((gp.getHeight() - spacing) / (buttonHeight + spacing)), 5, 5);
        }

        // rate
        if (materialSet) {
            g2d.setColor(displayBackgroundColor);
            g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 135, 110, 20, 8, 8);
            if (editingRate) {
                g2d.setColor(new Color(100, 104, 100));
                g2d.drawRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 135, 110, 20, 8, 8);
            }
            g2d.setColor(Color.WHITE);
            boolean showOverclockCursor = ((int) (System.currentTimeMillis() / 500) % 2) == 0 && editingRate;
            g2d.drawString(rateString + (showOverclockCursor ? "|" : ""), menuTopLeft.x + 15, menuTopLeft.y + 150);
        } else {
            g2d.setColor(Color.WHITE);
            g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 145, menuTopLeft.x + 100, menuTopLeft.y + 145);
        }
    }
}
