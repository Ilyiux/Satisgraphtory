package buildings.logistics;

import buildings.Building;
import main.GraphicsPanel;
import main.Main;
import main.PointDouble;
import main.Screen;
import recipes.Material;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Splitter extends Building {

    private BufferedImage image;

    private boolean isEfficient = true;

    public Splitter(Point position) {
        this.position = position;
        maxInConveyors = 1;
        maxOutConveyors = 3;
        maxInPipes = 0;
        maxOutPipes = 0;
        getImages();
    }

    private void getImages() {
        image = Main.getImageFromResources("/images/buildings/splitter.png");
    }

    public void update() {
        updateOutItems();
        updateInItems();
    }

    private void updateInItems() {
        inItems.clear();
        double amount = 0;
        Material type = null;
        for (Conveyor c : outConveyors) {
            if (!c.invalidState) {
                type = c.type;
                amount += c.outMaxRate;
            }
        }
        inItems.put(type, amount);
    }

    private void updateOutItems() {
        outConveyorRate.clear();
        outConveyorType.clear();
        outPipeRate.clear();
        outPipeType.clear();

        for (int c = 0; c < maxOutConveyors; c++) {
            outConveyorRate.add(-1.0);
            outConveyorType.add(null);
        }
        for (int p = 0; p < maxOutPipes; p++) {
            outPipeRate.add(-1.0);
            outPipeType.add(null);
        }

        double amount = 0;
        Material type = null;
        for (Conveyor c : inConveyors) {
            if (!c.invalidState) {
                type = c.type;
                amount += c.outRate;
            }
        }

        double totalOut = 0;
        isEfficient = true;
        for (Conveyor c : outConveyors) {
            totalOut += c.outMaxRate;
        }
        if (amount > totalOut) isEfficient = false;

        int full = 0;
        do {
            boolean anyFull = false;
            double maxPer = amount / (outConveyors.size() - full);
            for (int i = 0; i < outConveyors.size(); i++) {
                if (outConveyors.get(i).outMaxRate < maxPer && outConveyorRate.get(i) == -1) {
                    anyFull = true;
                    full++;
                    amount -= outConveyors.get(i).outMaxRate;
                    outConveyorRate.set(i, outConveyors.get(i).outMaxRate);
                    outConveyorType.set(i, type);
                }
            }

            if (!anyFull) {
                double remainderPer = amount / (outConveyors.size() - full);
                for (int i = 0; i < outConveyors.size(); i++) {
                    if (outConveyorRate.get(i) == -1) {
                        full ++;
                        outConveyorRate.set(i, remainderPer);
                        outConveyorType.set(i, type);
                    }
                }
            }
        } while (amount > 0 && full < outConveyors.size());
    }

    public void clicked(GraphicsPanel gp, int mx, int my) {
        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5), gp);
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 75);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 75);
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

        if (mx < menuTopLeft.x || mx > menuBottomRight.x || my < menuTopLeft.y || my > menuBottomRight.y)
            gp.closeBuildingMenu();

        if (mx > menuTopLeft.x + 105 && mx < menuTopLeft.x + 190 && my > menuTopLeft.y + 110 && my < menuTopLeft.y + 140) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 95 && my > menuTopLeft.y + 110 && my < menuTopLeft.y + 140) {
            gp.closeBuildingMenu();
            gp.addBuilding(new Splitter(position));
        }
    }

    public void typed(GraphicsPanel gp, int keyCode) {
        if (keyCode == KeyEvent.VK_X) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (keyCode == KeyEvent.VK_D) {
            gp.closeBuildingMenu();
            gp.addBuilding(new Splitter(position));
        }
    }

    public void draw(boolean greyedOut, Graphics2D g2d, GraphicsPanel gp) {
        Point start = Screen.convertToScreenPoint(new PointDouble(position.x, position.y), gp);
        Point end = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 1), gp);

        g2d.drawImage(image, start.x, start.y, end.x - start.x, end.y - start.y, null);

        if (!isEfficient) {
            g2d.setColor(Color.YELLOW);
        } else {
            g2d.setColor(Color.GREEN);
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
        String nameString = "Splitter";
        g2d.drawString(nameString, namePos.x, namePos.y);
    }

    public void drawMenu(Graphics2D g2d, GraphicsPanel gp) {
        g2d.setColor(new Color(0, 0, 0, 63));
        g2d.fillRect(0, 0, gp.getWidth(), gp.getHeight());

        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5), gp);
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 75);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 75);
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
        g2d.drawString("Splitter", menuTopLeft.x + 10, menuTopLeft.y + 25);
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 35, menuTopLeft.x + 190, menuTopLeft.y + 35);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(new Color(232, 79, 79));
        g2d.fillRoundRect(menuTopLeft.x + 105, menuTopLeft.y + 110, 85, 30, 5, 5);
        g2d.setColor(new Color(79, 79, 232));
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 110, 85, 30, 5, 5);
        g2d.setColor(new Color(30, 32, 30));
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Delete", menuTopLeft.x + 123, menuTopLeft.y + 130);
        g2d.drawString("Duplicate", menuTopLeft.x + 18, menuTopLeft.y + 130);
    }
}
